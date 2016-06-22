/* 
 * Copyright 2016 Patrik Karlsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.trixon.idd.db;

import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageProcessingException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import se.trixon.idd.Config;
import se.trixon.idd.db.manager.AlbumManager;
import se.trixon.idd.db.manager.AlbumRootManager;
import se.trixon.idd.db.manager.ImageManager;
import se.trixon.idl.shared.db.Album;
import se.trixon.idl.shared.db.AlbumRoot;
import se.trixon.idl.shared.db.Image;
import se.trixon.almond.util.Xlog;

/**
 *
 * @author Patrik Karlsson
 */
public class FileVisitor extends SimpleFileVisitor<Path> {

    private Long mAlbumId;
    private Long mAlbumRootId;
    private final Config mConfig = Config.getInstance();
    private int mCurrentDirLevel;
    private FileType mFileType;
    private boolean mInterrupted;
    private Path mSpecificPath;

    public FileVisitor() {
    }

    public boolean isInterrupted() {
        return mInterrupted;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (Thread.interrupted()) {
            mInterrupted = true;
            return FileVisitResult.TERMINATE;
        }

        mCurrentDirLevel = dir.getNameCount() - mConfig.getImageDirectoryLevel();
        if (mCurrentDirLevel == 1) {
            AlbumRoot albumRoot = new AlbumRoot();
            albumRoot.setLabel(dir.getFileName().toString());
            albumRoot.setSpecificPath(dir.toString());
            albumRoot.setStatus(0);
            albumRoot.setType(1);

            try {
                mAlbumRootId = AlbumRootManager.getInstance().insert(albumRoot);
            } catch (SQLException | ClassNotFoundException ex) {
                Logger.getLogger(FileVisitor.class.getName()).log(Level.SEVERE, null, ex);
            }
            mSpecificPath = dir;
            Xlog.timedOut("Adding album root: " + dir.toString());
        } else if (mCurrentDirLevel > 1) {
            Album album = new Album();
            album.setAlbumRootId(mAlbumRootId);
            album.setCaption(null);
            album.setCollection(null);
            album.setIcon(null);
            album.setRelativePath(IOUtils.DIR_SEPARATOR + mSpecificPath.relativize(dir).toString());

            try {
                mAlbumId = AlbumManager.getInstance().insert(album);
            } catch (ClassNotFoundException | SQLException ex) {
                Logger.getLogger(FileVisitor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (mCurrentDirLevel > 0 && isFileTypeSupported(file.toFile())) {
            Image image = new Image();
            image.setAlbumId(mAlbumId);
            image.setCategory(1);
            image.setStatus(1);
            image.setFileSize(attrs.size());
            image.setModificationDate(new Timestamp(attrs.lastModifiedTime().toMillis()));
            image.setName(file.getFileName().toString());
            //image.setUniqueHash(getMd5(file));
            try {
                MetadataLoader metadataLoader = new MetadataLoader(image, file.toFile(), mFileType);
            } catch (ImageProcessingException ex) {
                Logger.getLogger(FileVisitor.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                ImageManager.getInstance().insert(image);
            } catch (ClassNotFoundException | SQLException ex) {
                Logger.getLogger(FileVisitor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return FileVisitResult.CONTINUE;
    }

    private String getMd5(Path file) {
        String md5 = null;
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(file.toFile());
            md5 = DigestUtils.md5Hex(fileInputStream);
            fileInputStream.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileVisitor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileVisitor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return md5;
    }

    private boolean isFileTypeSupported(File file) throws IOException {
        boolean supported;
        mFileType = null;

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            mFileType = FileTypeDetector.detectFileType(bis);
            supported = ArrayUtils.contains(mConfig.getImageFormats(), mFileType.toString().toLowerCase());
        }

        return supported;
    }
}
