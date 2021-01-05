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
package se.trixon.idl.db;

import java.sql.Date;

/**
 *
 * @author Patrik Karlström
 */
public class Album {

    private Long mAlbumRootId;
    private String mCaption;
    private String mCollection;
    private Date mDate;
    private Integer mIcon;
    private Long mId;
    private String mRelativePath;

    public Album() {
    }

    public Long getAlbumRootId() {
        return mAlbumRootId;
    }

    public String getCaption() {
        return mCaption;
    }

    public String getCollection() {
        return mCollection;
    }

    public Date getDate() {
        return mDate;
    }

    public Integer getIcon() {
        return mIcon;
    }

    public Long getId() {
        return mId;
    }

    public String getRelativePath() {
        return mRelativePath;
    }

    public void setAlbumRootId(Long albumRootId) {
        mAlbumRootId = albumRootId;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public void setCollection(String collection) {
        mCollection = collection;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public void setIcon(Integer icon) {
        mIcon = icon;
    }

    public void setId(Long id) {
        mId = id;
    }

    public void setRelativePath(String relativePath) {
        mRelativePath = relativePath;
    }

}
