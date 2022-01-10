/* 
 * Copyright 2022 Patrik Karlström.
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
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import se.trixon.idd.Config;
import se.trixon.idd.db.manager.AlbumManager;
import se.trixon.idd.db.manager.AlbumRootManager;
import se.trixon.idd.db.manager.ImageManager;
import se.trixon.idl.FrameImage;
import se.trixon.idl.IddHelper;
import se.trixon.idl.db.Album;
import se.trixon.idl.db.AlbumRoot;

/**
 *
 * @author Patrik Karlström
 */
public class FileVisitor extends SimpleFileVisitor<Path> {

    private static final Logger LOGGER = Logger.getLogger(FileVisitor.class.getName());

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
                LOGGER.log(Level.SEVERE, null, ex);
            }
            mSpecificPath = dir;
            LOGGER.log(Level.INFO, "Adding album root: {0}", dir.toString());
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
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        if (mCurrentDirLevel > 0 && isFileTypeSupported(path.toFile())) {
            FrameImage frameImage = new FrameImage();
            frameImage.setAlbumId(mAlbumId);
            frameImage.setCategory(1);
            frameImage.setStatus(1);
            frameImage.setFileSize(attrs.size());
            frameImage.setModificationDate(new Timestamp(attrs.lastModifiedTime().toMillis()));
            frameImage.setName(path.getFileName().toString());
            frameImage.setUniqueHash(IddHelper.getMd5(path.toFile()));

            try {
                MetadataLoader metadataLoader = new MetadataLoader(frameImage, path.toFile(), mFileType);
            } catch (ImageProcessingException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }

            try {
                ImageManager.getInstance().insert(frameImage);
            } catch (ClassNotFoundException | SQLException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException ex) {
        LOGGER.log(Level.SEVERE, null, ex);

        return FileVisitResult.CONTINUE;
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
