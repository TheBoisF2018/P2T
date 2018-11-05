package com.p2t.p2t;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PictureHandler implements Runnable {
    private String result;
    private Image image;
    public PictureHandler(Image image) { this.image = image; }

    @Override
    public void run()
    {
        result = getDetectedTexts(image);
    }

    public String getResult()
    {
        return result;
    }

    private BatchAnnotateImagesResponse getRequest(Image image)
    {
        try {

            HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
            Vision.Builder visionBuilder = new Vision.Builder(httpTransport, new AndroidJsonFactory(), null);
            visionBuilder.setVisionRequestInitializer(new VisionRequestInitializer("AIzaSyD3jdGVzdjVsS5HfAYws_9XOQ8Zq-E7lHk"));
            Vision vision = visionBuilder.build();
            Feature feature = new Feature();
            feature.setType("DOCUMENT_TEXT_DETECTION");
            AnnotateImageRequest request = new AnnotateImageRequest();
            request.setImage(image);
            request.setFeatures(Collections.singletonList(feature));
            BatchAnnotateImagesRequest batchRequest = new BatchAnnotateImagesRequest();
            batchRequest.setRequests(Collections.singletonList(request));
            final Vision.Images.Annotate annotateRequest = vision.images().annotate(batchRequest);
            annotateRequest.setDisableGZipContent(true);
            BatchAnnotateImagesResponse r = annotateRequest.execute();

            return r;
        }
        catch(IOException e){}
        return null;
    }

    private String getDetectedTexts(Image image){
        BatchAnnotateImagesResponse response = getRequest(image);
        String str = "";
        if(response == null)
        {
            str = "Something went wrong";
            return str;
        }
        List<EntityAnnotation> texts = response.getResponses().get(0).getTextAnnotations();
        if (texts != null) {
            for (EntityAnnotation text : texts) {
                str = str + (String.format(Locale.getDefault(), "%s: %s", text.getLocale(), text.getDescription()));
                str = str + " ";
            }

        } else {
            str = "Cannot find text";
        }

        return str;
    }
}