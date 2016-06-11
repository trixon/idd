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

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDescriptor;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDescriptor;
import com.drew.metadata.exif.GpsDirectory;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.trixon.idl.shared.db.Image;

/**
 *
 * @author Patrik Karlsson
 */
public class MetadataLoader {

    private ExifIFD0Directory mExifIFD0Directory;
    private ExifSubIFDDescriptor mExifSubIFDDescriptor;
    private ExifSubIFDDirectory mExifSubIFDDirectory;
    private GpsDescriptor mGpsDescriptor;
    private GpsDirectory mGpsDirectory;

    public MetadataLoader(Image image, File file) throws IOException {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            mExifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            mExifSubIFDDescriptor = new ExifSubIFDDescriptor(mExifSubIFDDirectory);
            mExifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            mGpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            mGpsDescriptor = new GpsDescriptor(mGpsDirectory);

            image.setPosition(getPosition());
            image.setInformation(getInformation());
        } catch (ImageProcessingException ex) {
            Logger.getLogger(FileVisitor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private Image.Information getInformation() {
        Image.Information information = null;

        if (mExifSubIFDDirectory != null) {
            information = new Image.Information();
            information.setColorDepth(0);
            information.setColorModel(0);
            if (mExifSubIFDDirectory.containsTag(ExifDirectoryBase.TAG_DATETIME_ORIGINAL)) {
                information.setCreationDate(mExifSubIFDDirectory.getDate(ExifDirectoryBase.TAG_DATETIME_ORIGINAL).getTime());
            }
            if (mExifSubIFDDirectory.containsTag(ExifDirectoryBase.TAG_DATETIME_DIGITIZED)) {
                information.setDigitizationDate(mExifSubIFDDirectory.getDate(ExifDirectoryBase.TAG_DATETIME_DIGITIZED).getTime());

            }
//            information.setFormat(format);
            information.setHeigth(mExifSubIFDDirectory.getInteger(ExifDirectoryBase.TAG_EXIF_IMAGE_HEIGHT));
            information.setWidth(mExifSubIFDDirectory.getInteger(ExifDirectoryBase.TAG_EXIF_IMAGE_WIDTH));
            information.setOrientation(mExifSubIFDDirectory.getInteger(mExifIFD0Directory.TAG_ORIENTATION));
            information.setRating(-1);

//            System.out.println("orientation: " + mExifSubIFDDirectory.getObject(mExifIFD0Directory.TAG_ORIENTATION));

            //System.out.println(descriptor.getOrientationDescription());
        }

        return information;
    }

    private Image.Position getPosition() {
        Image.Position position = null;

        if (mGpsDirectory != null) {
            GeoLocation location = mGpsDirectory.getGeoLocation();
            if (location != null && !location.isZero()) {
                position = new Image.Position();
                position.setLatitude(mGpsDescriptor.getGpsLatitudeDescription());
                position.setLatitudeNumber(location.getLatitude());
                position.setLongitude(mGpsDescriptor.getGpsLongitudeDescription());
                position.setLongitudeNumber(location.getLongitude());
                position.setAccuracy(mGpsDirectory.getDoubleObject(GpsDirectory.TAG_DOP));
                position.setAltitude(mGpsDirectory.getDoubleObject(GpsDirectory.TAG_ALTITUDE));
                position.setOrientation(mGpsDirectory.getDoubleObject(GpsDirectory.TAG_IMG_DIRECTION));

//position.setDescription(descriptor);
//position.setRoll(Double.NaN);
//position.setTilt(Double.NaN);
            }
        }

        return position;
    }

}
