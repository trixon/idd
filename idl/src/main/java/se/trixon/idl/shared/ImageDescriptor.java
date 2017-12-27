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
package se.trixon.idl.shared;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Patrik Karlsson
 */
public class ImageDescriptor {

    private static final Gson GSON = new GsonBuilder()
            .setVersion(1.0)
            .serializeNulls()
            .setPrettyPrinting()
            .create();
    private static final Logger LOGGER = Logger.getLogger(ImageDescriptor.class.getName());
    @SerializedName("base64")
    private String mBase64;
    @SerializedName("image")
    private se.trixon.idl.shared.db.Image mImage;
    @SerializedName("path")
    private String mPath;

    public static ImageDescriptor fromJson(String json) throws IOException, JsonSyntaxException {
        ImageDescriptor descriptor = GSON.fromJson(json, ImageDescriptor.class);

        return descriptor;
    }

    public ImageDescriptor() {
    }

    public String getBase64() {
        return mBase64;
    }

    public byte[] getByteArray() {
        return Base64.decodeBase64(mBase64);
    }

    public se.trixon.idl.shared.db.Image getImage() {
        return mImage;
    }

    public java.awt.Image getImageAwt() {
        return getImageIcon().getImage();
    }

    public javafx.scene.image.Image getImageFx() {
        return new javafx.scene.image.Image(new ByteArrayInputStream(getByteArray()));
    }

    public ImageIcon getImageIcon() {
        return new ImageIcon(getByteArray());
    }

    public String getPath() {
        return mPath;
    }

    public void setBase64(String base64) {
        mBase64 = base64;
    }

    public void setBase64FromPath(String path) {
        try {
            mBase64 = Base64.encodeBase64String(FileUtils.readFileToByteArray(new File(path)));
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public void setImage(se.trixon.idl.shared.db.Image image) {
        mImage = image;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public String toJson() {
        return GSON.toJson(this);
    }
}
