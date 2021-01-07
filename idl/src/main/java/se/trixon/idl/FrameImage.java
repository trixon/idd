/*
 * Copyright 2021 Patrik Karlström.
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
package se.trixon.idl;

import com.google.gson.annotations.SerializedName;
import java.sql.Timestamp;
import org.apache.commons.lang3.ObjectUtils;

/**
 *
 * @author Patrik Karlström
 */
public class FrameImage {

    @SerializedName("album_id")
    private Long mAlbumId;
    @SerializedName("category")
    private Integer mCategory;
    @SerializedName("file_size")
    private Long mFileSize;
    @SerializedName("image_id")
    private Long mId;
    @SerializedName("information")
    private Information mInformation;
    @SerializedName("metadata")
    private Metadata mMetadata;
    @SerializedName("modification_date")
    private Timestamp mModificationDate;
    @SerializedName("modification_date_millis")
    private Long mModificationDateMillis;
    @SerializedName("name")
    private String mName;
    @SerializedName("path")
    private String mPath;
    @SerializedName("posititon")
    private Position mPosition;
    @SerializedName("status")
    private Integer mStatus;
    @SerializedName("unique_hash")
    private String mUniqueHash;

    public FrameImage() {
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

    public Long getModificationDateMillis() {
        return mModificationDateMillis;
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
        setModificationDateMillis(modificationDate.getTime());
    }

    public void setModificationDateMillis(Long modificationDateMillis) {
        mModificationDateMillis = modificationDateMillis;
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
        return "Image{"
                + "\n  AlbumId=" + mAlbumId
                + "\n  Category=" + mCategory
                + "\n  FileSize=" + mFileSize
                + "\n  Id=" + mId
                + "\n  ModificationDate=" + mModificationDate
                + "\n  ModificationDateMillis=" + mModificationDateMillis
                + "\n  Name=" + mName
                + "\n  Status=" + mStatus
                + "\n  UniqueHash=" + mUniqueHash
                + "\n"
                + "\n  Information=" + mInformation
                + "\n  Metadata=" + mMetadata
                + "\n  Position=" + mPosition
                + "\n}";
    }

    public static class Information {

        @SerializedName("color_depth")
        private Integer mColorDepth;
        @SerializedName("color_model")
        private Integer mColorModel;
        @SerializedName("creation_date")
        private Timestamp mCreationDate;
        @SerializedName("creation_date_millis")
        private Long mCreationDateMillis;
        @SerializedName("digitization_date")
        private Timestamp mDigitizationDate;
        @SerializedName("digitization_date_millis")
        private Long mDigitizationDateMillis;
        @SerializedName("format")
        private String mFormat;
        @SerializedName("height")
        private Integer mHeigth;
        @SerializedName("image_id")
        private Long mImageId;
        @SerializedName("orientation")
        private Integer mOrientation;
        @SerializedName("rating")
        private Integer mRating;
        @SerializedName("width")
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

        public Long getCreationDateMillis() {
            return mCreationDateMillis;
        }

        public Timestamp getDigitizationDate() {
            return mDigitizationDate;
        }

        public Long getDigitizationDateMillis() {
            return mDigitizationDateMillis;
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
            return ObjectUtils.anyNotNull(
                    mColorDepth,
                    mColorModel,
                    mCreationDate,
                    mDigitizationDate,
                    mFormat,
                    mHeigth,
                    mOrientation,
                    mRating,
                    mWidth
            );
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
                mCreationDateMillis = millis;
            } else {
                mCreationDate = null;
                mCreationDateMillis = null;
            }
        }

        public void setCreationDate(Timestamp creationDate) {
            mCreationDate = creationDate;
            setCreationDateMillis(creationDate.getTime());
        }

        public void setCreationDateMillis(Long creationDateMillis) {
            mCreationDateMillis = creationDateMillis;
        }

        public void setDigitizationDate(Long millis) {
            if (millis != null) {
                mDigitizationDate = new Timestamp(millis);
                mDigitizationDateMillis = millis;
            } else {
                mDigitizationDate = null;
                mDigitizationDateMillis = null;
            }
        }

        public void setDigitizationDate(Timestamp digitizationDate) {
            mDigitizationDate = digitizationDate;
            setDigitizationDate(digitizationDate.getTime());
        }

        public void setDigitizationDateMillis(Long digitizationDateMillis) {
            mDigitizationDateMillis = digitizationDateMillis;
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
            return "Information{"
                    + "\n    ColorDepth=" + mColorDepth
                    + "\n    ColorModel=" + mColorModel
                    + "\n    CreationDate=" + mCreationDate
                    + "\n    CreationDateMillis=" + mCreationDateMillis
                    + "\n    DigitizationDate=" + mDigitizationDate
                    + "\n    DigitizationDateMillis=" + mDigitizationDateMillis
                    + "\n    Format=" + mFormat
                    + "\n    Heigth=" + mHeigth
                    + "\n    ImageId=" + mImageId
                    + "\n    Orientation=" + mOrientation
                    + "\n    Rating=" + mRating
                    + "\n    Width=" + mWidth
                    + "\n  }";
        }

    }

    public static class Metadata {

        @SerializedName("aperture")
        private Double mAperture;
        @SerializedName("exposure_mode")
        private Integer mExposureMode;
        @SerializedName("exposure_program")
        private Integer mExposureProgram;
        @SerializedName("exposure_time")
        private Double mExposureTime;
        @SerializedName("flash")
        private Integer mFlash;
        @SerializedName("focal_length")
        private Double mFocalLength;
        @SerializedName("focal_length35")
        private Double mFocalLength35;
        @SerializedName("image_id")
        private Long mImageId;
        @SerializedName("lens")
        private String mLens;
        @SerializedName("make")
        private String mMake;
        @SerializedName("metering_mode")
        private Integer mMeteringMode;
        @SerializedName("model")
        private String mModel;
        @SerializedName("sensitivity")
        private Integer mSensitivity;
        @SerializedName("subject_distance")
        private String mSubjectDistance;
        @SerializedName("subject_distance_category")
        private Integer mSubjectDistanceCategory;
        @SerializedName("white_balance")
        private Integer mWhiteBalance;
        @SerializedName("white_balance_color_temperature")
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
            return ObjectUtils.anyNotNull(
                    mMake,
                    mModel,
                    mLens,
                    mAperture,
                    mFocalLength,
                    mFocalLength35,
                    mExposureTime,
                    mExposureProgram,
                    mExposureMode,
                    mSensitivity,
                    mFlash,
                    mWhiteBalance,
                    mWhiteBalanceColorTemperature,
                    mMeteringMode,
                    mSubjectDistance,
                    mSubjectDistanceCategory
            );
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
            return "Metadata{"
                    + "\n    Aperture=" + mAperture
                    + "\n    ExposureMode=" + mExposureMode
                    + "\n    ExposureProgram=" + mExposureProgram
                    + "\n    ExposureTime=" + mExposureTime
                    + "\n    Flash=" + mFlash
                    + "\n    FocalLength=" + mFocalLength
                    + "\n    FocalLength35=" + mFocalLength35
                    + "\n    ImageId=" + mImageId
                    + "\n    Lens=" + mLens
                    + "\n    Make=" + mMake
                    + "\n    MeteringMode=" + mMeteringMode
                    + "\n    Model=" + mModel
                    + "\n    Sensitivity=" + mSensitivity
                    + "\n    SubjectDistance=" + mSubjectDistance
                    + "\n    SubjectDistanceCategory=" + mSubjectDistanceCategory
                    + "\n    WhiteBalance=" + mWhiteBalance
                    + "\n    WhiteBalanceColorTemperature=" + mWhiteBalanceColorTemperature
                    + "\n  }";
        }

    }

    public static class Position {

        @SerializedName("accuracy")
        private Double mAccuracy;
        @SerializedName("altitude")
        private Double mAltitude;
        @SerializedName("description")
        private String mDescription;
        @SerializedName("image_id")
        private Long mImageId;
        @SerializedName("latitude")
        private String mLatitude;
        @SerializedName("latitude_number")
        private Double mLatitudeNumber;
        @SerializedName("longitude")
        private String mLongitude;
        @SerializedName("longitude_number")
        private Double mLongitudeNumber;
        @SerializedName("orientation")
        private Double mOrientation;
        @SerializedName("roll")
        private Double mRoll;
        @SerializedName("tilt")
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
            return ObjectUtils.anyNotNull(
                    mAccuracy,
                    mAltitude,
                    mDescription,
                    mLatitude,
                    mLatitudeNumber,
                    mLongitude,
                    mLongitudeNumber,
                    mOrientation,
                    mRoll,
                    mTilt
            );
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
            return "Position{"
                    + "\n    Accuracy=" + mAccuracy
                    + "\n    Altitude=" + mAltitude
                    + "\n    Description=" + mDescription
                    + "\n    ImageId=" + mImageId
                    + "\n    Latitude=" + mLatitude
                    + "\n    LatitudeNumber=" + mLatitudeNumber
                    + "\n    Longitude=" + mLongitude
                    + "\n    LongitudeNumber=" + mLongitudeNumber
                    + "\n    Orientation=" + mOrientation
                    + "\n    Roll=" + mRoll
                    + "\n    Tilt=" + mTilt
                    + "\n  }";
        }
    }
}
