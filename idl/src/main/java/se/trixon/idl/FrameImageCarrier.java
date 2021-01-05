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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import se.trixon.almond.util.GraphicsHelper;

/**
 *
 * @author Patrik Karlström
 */
public class FrameImageCarrier {

    private static final Gson GSON = new GsonBuilder()
            .setVersion(1.0)
            .serializeNulls()
            .setPrettyPrinting()
            .setLenient()
            .create();
    private static final Logger LOGGER = Logger.getLogger(FrameImageCarrier.class.getName());
    @SerializedName("base64")
    private String mBase64;
    @SerializedName("frame_image")
    private se.trixon.idl.FrameImage mFrameImage;
    @SerializedName("md5")
    private String mMd5;
    @SerializedName("path")
    private String mPath;

    public static FrameImageCarrier fromJson(String json) throws IOException, JsonSyntaxException {
        FrameImageCarrier descriptor = GSON.fromJson(json, FrameImageCarrier.class);

        return descriptor;
    }

    public FrameImageCarrier() {
    }

    public FrameImageCarrier(FrameImage frameImage, String path) {
        mFrameImage = frameImage;
        mPath = path;
        setBase64FromPath(path);
        mMd5 = IddHelper.getMd5(new File(path));
    }

    public String getBase64() {
        return mBase64;
    }

    public BufferedImage getBufferedImage() {
        try {
            return ImageIO.read(getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(FrameImageCarrier.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public byte[] getByteArray() {
        return Base64.getDecoder().decode(mBase64);
    }

    public FrameImage getFrameImage() {
        return mFrameImage;
    }

    public java.awt.Image getImageAwt() {
        return getImageIcon().getImage();
    }

    public javafx.scene.image.Image getImageFx() {
        return new javafx.scene.image.Image(getInputStream());
    }

    public ImageIcon getImageIcon() {
        return new ImageIcon(getByteArray());
    }

    public ByteArrayInputStream getInputStream() {
        return new ByteArrayInputStream(getByteArray());
    }

    public String getMd5() {
        return mMd5;
    }

    public String getPath() {
        return mPath;
    }

    public BufferedImage getRotatedBufferedImage() {
        return GraphicsHelper.rotate(getBufferedImage(), getFrameImage().getInformation().getOrientation());
    }

    public javafx.scene.image.Image getRotatedImageFx() {
        return SwingFXUtils.toFXImage(getRotatedBufferedImage(), null);
    }

    public boolean hasValidMd5() {
        String md5 = null;

        try {
            md5 = IddHelper.getMd5(IOUtils.toByteArray(getInputStream()));
        } catch (IOException ex) {
            Logger.getLogger(FrameImageCarrier.class.getName()).log(Level.SEVERE, null, ex);
        }

        return StringUtils.equalsIgnoreCase(md5, getMd5());
    }

    public boolean save(File file) {
        ByteArrayInputStream inputStream = getInputStream();

        try {
            FileUtils.copyInputStreamToFile(inputStream, file);
            inputStream.close();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    public void setBase64(String base64) {
        mBase64 = base64;
    }

    public void setBase64FromPath(String path) {
        try {
            mBase64 = Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(new File(path)));
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public void setFrameImage(FrameImage frameImage) {
        mFrameImage = frameImage;
    }

    public void setMd5(String md5) {
        mMd5 = md5;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public String toJson() {
        return GSON.toJson(this);
    }
}
