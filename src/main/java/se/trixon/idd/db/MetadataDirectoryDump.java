/* 
 * Copyright 2017 Patrik Karlsson.
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

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Patrik Karlsson
 */
public class MetadataDirectoryDump {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ImageProcessingException, IOException {
        File[] files = new File[]{new File("/atlas/eget/foto/original/2016/05/2016-05-23/IMG_5016.JPG"), new File("/atlas/eget/foto/original/2009/01/2009-01-02/2009-01-02__001.JPG")};

        for (File file : files) {
            Metadata metadata = ImageMetadataReader.readMetadata(file);

            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    System.out.format("[%s] - %s = %s\n", directory.getName(), tag.getTagName(), tag.getDescription());
                }
                if (directory.hasErrors()) {
                    for (String error : directory.getErrors()) {
                        System.err.format("ERROR: %s", error);
                    }
                }
            }
            System.out.println("");
            System.out.println("");
            System.out.println("");
            System.out.println("");
        }
    }
}
