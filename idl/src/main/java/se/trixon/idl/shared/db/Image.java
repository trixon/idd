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
package se.trixon.idl.shared.db;

import java.sql.Timestamp;

/**
 *
 * @author Patrik Karlsson
 */
public class Image {

    private Long mAlbumId;
    private Integer mCategory;
    private Long mFileSize;
    private Long mId;
    private Information mInformation;
    private Metadata mMetadata;
    private Timestamp mModificationDate;
    private String mName;
    private String mPath;
    private Position mPosition;
    private Integer mStatus;
    private String mUniqueHash;

    public Image() {
        mInformation = new Information();
        mPosition = new Position();
        mMetadata = new Metadata();
    }

    public Long getAlbumId() {
        return mAlbumId;
    }

    public Integer getCategory() {
        return mCategory;
    }

    public Long getFileSize() {
        return mFileSize;
    }

    public Long getId() {
        return mId;
    }

    public Information getInformation() {
        return mInformation;
    }

    public Metadata getMetadata() {
        return mMetadata;
    }

    public Timestamp getModificationDate() {
        return mModificationDate;
    }

    public String getName() {
        return mName;
    }

    public String getPath() {
        return mPath;
    }

    public Position getPosition() {
        return mPosition;
    }

    public Integer getStatus() {
        return mStatus;
    }

    public String getUniqueHash() {
        return mUniqueHash;
    }

    public void setAlbumId(Long albumId) {
        mAlbumId = albumId;
    }

    public void setCategory(Integer category) {
        mCategory = category;
    }

    public void setFileSize(Long fileSize) {
        mFileSize = fileSize;
    }

    public void setId(Long id) {
        mId = id;
    }

    public void setInformation(Information information) {
        mInformation = information;
    }

    public void setMetadata(Metadata metadata) {
        mMetadata = metadata;
    }

    public void setModificationDate(Timestamp modificationDate) {
        mModificationDate = modificationDate;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public void setPosition(Position position) {
        mPosition = position;
    }

    public void setStatus(Integer status) {
        mStatus = status;
    }

    public void setUniqueHash(String uniqueHash) {
        mUniqueHash = uniqueHash;
    }

    @Override
    public String toString() {
        return "Image{" + "mAlbumId=" + mAlbumId + ", mCategory=" + mCategory + ", mFileSize=" + mFileSize + ", mId=" + mId + ", \nmInformation=" + mInformation + ", \nmMetadata=" + mMetadata + ", \nmModificationDate=" + mModificationDate + ", mName=" + mName + ", \nmPosition=" + mPosition + ", \nmStatus=" + mStatus + ", mUniqueHash=" + mUniqueHash + '}';
    }

    public static class Information {

        private Integer mColorDepth;
        private Integer mColorModel;
        private Timestamp mCreationDate;
        private Timestamp mDigitizationDate;
        private String mFormat;
        private Integer mHeigth;
        private Long mImageId;
        private Integer mOrientation;
        private Integer mRating;
        private Integer mWidth;

        public Information() {
        }

        public Integer getColorDepth() {
            return mColorDepth;
        }

        public Integer getColorModel() {
            return mColorModel;
        }

        public Timestamp getCreationDate() {
            return mCreationDate;
        }

        public Timestamp getDigitizationDate() {
            return mDigitizationDate;
        }

        public String getFormat() {
            return mFormat;
        }

        public Integer getHeigth() {
            return mHeigth;
        }

        public Long getImageId() {
            return mImageId;
        }

        public Integer getOrientation() {
            return mOrientation;
        }

        public Integer getRating() {
            return mRating;
        }

        public Integer getWidth() {
            return mWidth;
        }

        public boolean hasData() {
            return mColorDepth != null
                    || mColorModel != null
                    || mCreationDate != null
                    || mDigitizationDate != null
                    || mFormat != null
                    || mHeigth != null
                    || mOrientation != null
                    || mRating != null
                    || mWidth != null;
        }

        public void setColorDepth(Integer colorDepth) {
            mColorDepth = colorDepth;
        }

        public void setColorModel(Integer colorModel) {
            mColorModel = colorModel;
        }

        public void setCreationDate(Long millis) {
            if (millis != null) {
                mCreationDate = new Timestamp(millis);
            } else {
                mCreationDate = null;
            }
        }

        public void setCreationDate(Timestamp creationDate) {
            mCreationDate = creationDate;
        }

        public void setDigitizationDate(Long millis) {
            if (millis != null) {
                mDigitizationDate = new Timestamp(millis);
            } else {
                mDigitizationDate = null;
            }
        }

        public void setDigitizationDate(Timestamp digitizationDate) {
            mDigitizationDate = digitizationDate;
        }

        public void setFormat(String format) {
            mFormat = format;
        }

        public void setHeigth(Integer heigth) {
            mHeigth = heigth;
        }

        public void setImageId(Long imageId) {
            mImageId = imageId;
        }

        public void setOrientation(Integer orientation) {
            mOrientation = orientation;
        }

        public void setRating(Integer rating) {
            mRating = rating;
        }

        public void setWidth(Integer width) {
            mWidth = width;
        }

        @Override
        public String toString() {
            return "Information{" + "mColorDepth=" + mColorDepth + ", mColorModel=" + mColorModel + ", mCreationDate=" + mCreationDate + ", mDigitizationDate=" + mDigitizationDate + ", mFormat=" + mFormat + ", mHeigth=" + mHeigth + ", mImageId=" + mImageId + ", mOrientation=" + mOrientation + ", mRating=" + mRating + ", mWidth=" + mWidth + '}';
        }

    }

    public static class Metadata {

        private Double mAperture;
        private Integer mExposureMode;
        private Integer mExposureProgram;
        private Double mExposureTime;
        private Integer mFlash;
        private Double mFocalLength;
        private Double mFocalLength35;
        private Long mImageId;
        private String mLens;
        private String mMake;
        private Integer mMeteringMode;
        private String mModel;
        private Integer mSensitivity;
        private String mSubjectDistance;
        private Integer mSubjectDistanceCategory;
        private Integer mWhiteBalance;
        private Integer mWhiteBalanceColorTemperature;

        public Metadata() {
        }

        public Double getAperture() {
            return mAperture;
        }

        public Integer getExposureMode() {
            return mExposureMode;
        }

        public Integer getExposureProgram() {
            return mExposureProgram;
        }

        public Double getExposureTime() {
            return mExposureTime;
        }

        public Integer getFlash() {
            return mFlash;
        }

        public Double getFocalLength() {
            return mFocalLength;
        }

        public Double getFocalLength35() {
            return mFocalLength35;
        }

        public Long getImageId() {
            return mImageId;
        }

        public String getLens() {
            return mLens;
        }

        public String getMake() {
            return mMake;
        }

        public Integer getMeteringMode() {
            return mMeteringMode;
        }

        public String getModel() {
            return mModel;
        }

        public Integer getSensitivity() {
            return mSensitivity;
        }

        public String getSubjectDistance() {
            return mSubjectDistance;
        }

        public Integer getSubjectDistanceCategory() {
            return mSubjectDistanceCategory;
        }

        public Integer getWhiteBalance() {
            return mWhiteBalance;
        }

        public Integer getWhiteBalanceColorTemperature() {
            return mWhiteBalanceColorTemperature;
        }

        public boolean hasData() {
            return mMake != null
                    || mModel != null
                    || mLens != null
                    || mAperture != null
                    || mFocalLength != null
                    || mFocalLength35 != null
                    || mExposureTime != null
                    || mExposureProgram != null
                    || mExposureMode != null
                    || mSensitivity != null
                    || mFlash != null
                    || mWhiteBalance != null
                    || mWhiteBalanceColorTemperature != null
                    || mMeteringMode != null
                    || mSubjectDistance != null
                    || mSubjectDistanceCategory != null;
        }

        public void setAperture(Double aperture) {
            mAperture = aperture;
        }

        public void setExposureMode(Integer exposureMode) {
            mExposureMode = exposureMode;
        }

        public void setExposureProgram(Integer exposureProgram) {
            mExposureProgram = exposureProgram;
        }

        public void setExposureTime(Double exposureTime) {
            mExposureTime = exposureTime;
        }

        public void setFlash(Integer flash) {
            mFlash = flash;
        }

        public void setFocalLength(Double focalLength) {
            mFocalLength = focalLength;
        }

        public void setFocalLength35(Double focalLength35) {
            mFocalLength35 = focalLength35;
        }

        public void setImageId(Long imageId) {
            mImageId = imageId;
        }

        public void setLens(String lens) {
            mLens = lens;
        }

        public void setMake(String make) {
            mMake = make;
        }

        public void setMeteringMode(Integer meteringMode) {
            mMeteringMode = meteringMode;
        }

        public void setModel(String model) {
            mModel = model;
        }

        public void setSensitivity(Integer sensitivity) {
            mSensitivity = sensitivity;
        }

        public void setSubjectDistance(String subjectDistance) {
            mSubjectDistance = subjectDistance;
        }

        public void setSubjectDistanceCategory(Integer subjectDistanceCategory) {
            mSubjectDistanceCategory = subjectDistanceCategory;
        }

        public void setWhiteBalance(Integer whiteBalance) {
            mWhiteBalance = whiteBalance;
        }

        public void setWhiteBalanceColorTemperature(Integer whiteBalanceColorTemperature) {
            mWhiteBalanceColorTemperature = whiteBalanceColorTemperature;
        }

        @Override
        public String toString() {
            return "Metadata{" + "mAperture=" + mAperture + ", mExposureMode=" + mExposureMode + ", mExposureProgram=" + mExposureProgram + ", mExposureTime=" + mExposureTime + ", mFlash=" + mFlash + ", mFocalLength=" + mFocalLength + ", mFocalLength35=" + mFocalLength35 + ", mImageId=" + mImageId + ", mLens=" + mLens + ", mMake=" + mMake + ", mMeteringMode=" + mMeteringMode + ", mModel=" + mModel + ", mSensitivity=" + mSensitivity + ", mSubjectDistance=" + mSubjectDistance + ", mSubjectDistanceCategory=" + mSubjectDistanceCategory + ", mWhiteBalance=" + mWhiteBalance + ", mWhiteBalanceColorTemperature=" + mWhiteBalanceColorTemperature + '}';
        }

    }

    public static class Position {

        private Double mAccuracy;
        private Double mAltitude;
        private String mDescription;
        private Long mImageId;
        private String mLatitude;
        private Double mLatitudeNumber;
        private String mLongitude;
        private Double mLongitudeNumber;
        private Double mOrientation;
        private Double mRoll;
        private Double mTilt;

        public Position() {
        }

        public Double getAccuracy() {
            return mAccuracy;
        }

        public Double getAltitude() {
            return mAltitude;
        }

        public String getDescription() {
            return mDescription;
        }

        public Long getImageId() {
            return mImageId;
        }

        public String getLatitude() {
            return mLatitude;
        }

        public Double getLatitudeNumber() {
            return mLatitudeNumber;
        }

        public String getLongitude() {
            return mLongitude;
        }

        public Double getLongitudeNumber() {
            return mLongitudeNumber;
        }

        public Double getOrientation() {
            return mOrientation;
        }

        public Double getRoll() {
            return mRoll;
        }

        public Double getTilt() {
            return mTilt;
        }

        public boolean hasData() {
            return mAccuracy != null
                    || mAltitude != null
                    || mDescription != null
                    || mLatitude != null
                    || mLatitudeNumber != null
                    || mLongitude != null
                    || mLongitudeNumber != null
                    || mOrientation != null
                    || mRoll != null
                    || mTilt != null;
        }

        public void setAccuracy(Double accuracy) {
            mAccuracy = accuracy;
        }

        public void setAltitude(Double altitude) {
            mAltitude = altitude;
        }

        public void setDescription(String description) {
            mDescription = description;
        }

        public void setImageId(Long imageId) {
            mImageId = imageId;
        }

        public void setLatitude(String latitude) {
            mLatitude = latitude;
        }

        public void setLatitudeNumber(Double latitudeNumber) {
            mLatitudeNumber = latitudeNumber;
        }

        public void setLongitude(String longitude) {
            mLongitude = longitude;
        }

        public void setLongitudeNumber(Double longitudeNumber) {
            mLongitudeNumber = longitudeNumber;
        }

        public void setOrientation(Double orientation) {
            mOrientation = orientation;
        }

        public void setRoll(Double roll) {
            mRoll = roll;
        }

        public void setTilt(Double tilt) {
            mTilt = tilt;
        }

        @Override
        public String toString() {
            return "Position{" + "mAccuracy=" + mAccuracy + ", mAltitude=" + mAltitude + ", mDescription=" + mDescription + ", mImageId=" + mImageId + ", mLatitude=" + mLatitude + ", mLatitudeNumber=" + mLatitudeNumber + ", mLongitude=" + mLongitude + ", mLongitudeNumber=" + mLongitudeNumber + ", mOrientation=" + mOrientation + ", mRoll=" + mRoll + ", mTilt=" + mTilt + '}';
        }
    }
}
