/* 
 * Copyright 2018 Patrik Karlström.
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
package se.trixon.idl.db;

/**
 *
 * @author Patrik Karlström
 */
public class AlbumRoot {

    private Long mId;
    private String mIdentifier;
    private String mLabel;
    private String mSpecificPath;
    private Integer mStatus;
    private Integer mType;

    public AlbumRoot() {
    }

    public Long getId() {
        return mId;
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    public String getLabel() {
        return mLabel;
    }

    public String getSpecificPath() {
        return mSpecificPath;
    }

    public Integer getStatus() {
        return mStatus;
    }

    public Integer getType() {
        return mType;
    }

    public void setId(Long id) {
        mId = id;
    }

    public void setIdentifier(String identifier) {
        mIdentifier = identifier;
    }

    public void setLabel(String label) {
        mLabel = label;
    }

    public void setSpecificPath(String specificPath) {
        mSpecificPath = specificPath;
    }

    public void setStatus(Integer status) {
        mStatus = status;
    }

    public void setType(Integer type) {
        mType = type;
    }

}
