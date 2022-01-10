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
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Descriptor;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDescriptor;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDescriptor;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.jpeg.JpegDescriptor;
import com.drew.metadata.jpeg.JpegDirectory;
import java.io.File;
import java.io.IOException;
import se.trixon.idl.FrameImage;

/**
 *
 * @author Patrik Karlström
 */
public class MetadataLoader {

    private final ExifIFD0Descriptor mExifIFD0Descriptor;
    private final ExifIFD0Directory mExifIFD0Directory;
    private final ExifSubIFDDescriptor mExifSubIFDDescriptor;
    private final ExifSubIFDDirectory mExifSubIFDDirectory;
    private final FileType mFileType;
    private final GpsDescriptor mGpsDescriptor;
    private final GpsDirectory mGpsDirectory;
    private final FrameImage mFrameImage;
    private final JpegDescriptor mJpegDescriptor;
    private final JpegDirectory mJpegDirectory;

    public MetadataLoader(FrameImage frameImage, File file, FileType fileType) throws IOException, ImageProcessingException {
        mFrameImage = frameImage;
        mFileType = fileType;
        Metadata metadata = ImageMetadataReader.readMetadata(file);

        mExifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        mExifIFD0Descriptor = new ExifIFD0Descriptor(mExifIFD0Directory);

        mExifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        mExifSubIFDDescriptor = new ExifSubIFDDescriptor(mExifSubIFDDirectory);

        mGpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        mGpsDescriptor = new GpsDescriptor(mGpsDirectory);

        mJpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);
        mJpegDescriptor = new JpegDescriptor(mJpegDirectory);

        loadPosition();
        loadInformation();
        loadMetadata();
    }

    private void loadInformation() {
        FrameImage.Information information = mFrameImage.getInformation();

        information.setRating(-1);
        information.setFormat(mFileType.toString());
        if (mExifSubIFDDirectory != null) {
            if (mExifSubIFDDirectory.containsTag(ExifDirectoryBase.TAG_DATETIME_ORIGINAL)) {
                information.setCreationDate(mExifSubIFDDirectory.getDate(ExifDirectoryBase.TAG_DATETIME_ORIGINAL).getTime());
            }
            if (mExifSubIFDDirectory.containsTag(ExifDirectoryBase.TAG_DATETIME_DIGITIZED)) {
                information.setDigitizationDate(mExifSubIFDDirectory.getDate(ExifDirectoryBase.TAG_DATETIME_DIGITIZED).getTime());
            }

            information.setWidth(mExifSubIFDDirectory.getInteger(ExifDirectoryBase.TAG_EXIF_IMAGE_WIDTH));
            information.setHeigth(mExifSubIFDDirectory.getInteger(ExifDirectoryBase.TAG_EXIF_IMAGE_HEIGHT));
            //TODO setColorModel
            //TODO setColorDepth
        }

        if (mExifIFD0Directory != null) {
            information.setOrientation(mExifIFD0Directory.getInteger(ExifIFD0Directory.TAG_ORIENTATION));
        }

        if (mJpegDirectory != null) {
        }
    }

    private void loadMetadata() {
        FrameImage.Metadata metadata = mFrameImage.getMetadata();

        if (mExifIFD0Directory != null) {
            metadata.setMake(mExifIFD0Directory.getString(ExifIFD0Directory.TAG_MAKE));
            metadata.setModel(mExifIFD0Directory.getString(ExifIFD0Directory.TAG_MODEL));
        }
        if (mExifSubIFDDirectory != null) {
            metadata.setLens(mExifSubIFDDirectory.getString(ExifSubIFDDirectory.TAG_LENS_MODEL));
            metadata.setAperture(mExifSubIFDDirectory.getDoubleObject(ExifSubIFDDirectory.TAG_APERTURE));
            metadata.setFocalLength(mExifSubIFDDirectory.getDoubleObject(ExifSubIFDDirectory.TAG_FOCAL_LENGTH));
            metadata.setFocalLength35(mExifSubIFDDirectory.getDoubleObject(ExifSubIFDDirectory.TAG_35MM_FILM_EQUIV_FOCAL_LENGTH));
            metadata.setExposureTime(mExifSubIFDDirectory.getDoubleObject(ExifSubIFDDirectory.TAG_EXPOSURE_TIME));
            metadata.setExposureProgram(mExifSubIFDDirectory.getInteger(ExifSubIFDDirectory.TAG_EXPOSURE_PROGRAM));
            metadata.setExposureMode(mExifSubIFDDirectory.getInteger(ExifSubIFDDirectory.TAG_EXPOSURE_MODE));
            metadata.setSensitivity(mExifSubIFDDirectory.getInteger(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT));
            metadata.setFlash(mExifSubIFDDirectory.getInteger(ExifSubIFDDirectory.TAG_FLASH));
            metadata.setWhiteBalance(mExifSubIFDDirectory.getInteger(ExifSubIFDDirectory.TAG_WHITE_BALANCE_MODE));
            metadata.setMeteringMode(mExifSubIFDDirectory.getInteger(ExifSubIFDDirectory.TAG_METERING_MODE));

            //TODO setWhiteBalanceColorTemperature
            //TODO setSubjectDistance;
            //TODO setSubjectDistanceCategory
        }
    }

    private void loadPosition() {
        FrameImage.Position position = mFrameImage.getPosition();

        if (mGpsDirectory != null) {
            GeoLocation location = mGpsDirectory.getGeoLocation();
            if (location != null && !location.isZero()) {
                position.setLatitude(mGpsDescriptor.getGpsLatitudeDescription());
                position.setLatitudeNumber(location.getLatitude());
                position.setLongitude(mGpsDescriptor.getGpsLongitudeDescription());
                position.setLongitudeNumber(location.getLongitude());
                position.setAccuracy(mGpsDirectory.getDoubleObject(GpsDirectory.TAG_DOP));
                position.setAltitude(mGpsDirectory.getDoubleObject(GpsDirectory.TAG_ALTITUDE));
                position.setOrientation(mGpsDirectory.getDoubleObject(GpsDirectory.TAG_IMG_DIRECTION));

                //TODO setDescription
                //TODO setRoll
                //TODO setTilt
            }
        }
    }
}
