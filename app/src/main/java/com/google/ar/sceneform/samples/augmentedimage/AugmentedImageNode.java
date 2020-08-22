/*
 * Copyright 2018 Google LLC
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

package com.google.ar.sceneform.samples.augmentedimage;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Node for rendering an augmented image. The image is framed by placing the virtual picture frame
 * at the corners of the augmented image trackable.
 */
@SuppressWarnings({"AndroidApiChecker"})
public class AugmentedImageNode extends AnchorNode {

  private static final String TAG = "AugmentedImageNode";

  // The augmented image represented by this node.
  private AugmentedImage image;

  // Models of the 4 corners.  We use completable futures here to simplify
  // the error handling and asynchronous loading.  The loading is started with the
  // first construction of an instance, and then used when the image is set.
  private static CompletableFuture<ModelRenderable> ulCorner;
  private static CompletableFuture<ModelRenderable> urCorner;
  private static CompletableFuture<ModelRenderable> lrCorner;
  private static CompletableFuture<ModelRenderable> llCorner;
  private static CompletableFuture<ModelRenderable> lion;
  private static CompletableFuture<ModelRenderable> ambulance;
  private static CompletableFuture<ModelRenderable> bulldozer;
  private static CompletableFuture<ModelRenderable> camel;
  private static CompletableFuture<ModelRenderable> cat;
  private static CompletableFuture<ModelRenderable> cheetah;
  private static CompletableFuture<ModelRenderable> dog;
  private static CompletableFuture<ModelRenderable> firetruck;
  private static CompletableFuture<ModelRenderable> fish;
  private static CompletableFuture<ModelRenderable> fox;
  private static CompletableFuture<ModelRenderable> militarytruck;
  private static CompletableFuture<ModelRenderable> policecar;
  private static CompletableFuture<ModelRenderable> schoolbus;
  private static CompletableFuture<ModelRenderable> zebra;


  private Map<String, CompletableFuture<ModelRenderable>> mapp = new HashMap<>();

  public AugmentedImageNode(Context context) {
    // Upon construction, start loading the models for the corners of the frame.
    if (ulCorner == null) {
      ulCorner =
          ModelRenderable.builder()
              .setSource(context, Uri.parse("models/frame_upper_left.sfb"))
              .build();
      urCorner =
          ModelRenderable.builder()
              .setSource(context, Uri.parse("models/frame_upper_right.sfb"))
              .build();
      llCorner =
          ModelRenderable.builder()
              .setSource(context, Uri.parse("models/frame_lower_left.sfb"))
              .build();
      lrCorner =
          ModelRenderable.builder()
              .setSource(context, Uri.parse("models/frame_lower_right.sfb"))
              .build();

      lion = ModelRenderable.builder()
              .setSource(context, Uri.parse("Lion.sfb"))
              .build();

      ambulance = ModelRenderable.builder()
              .setSource(context, Uri.parse("Ambulance.sfb"))
              .build();
      bulldozer = ModelRenderable.builder()
              .setSource(context, Uri.parse("Bulldozer_1375.sfb"))
              .build();
      camel = ModelRenderable.builder()
              .setSource(context, Uri.parse("BactrianCamel.sfb"))
              .build();
      cat = ModelRenderable.builder()
              .setSource(context, Uri.parse("Mesh_Cat.sfb"))
              .build();
      cheetah = ModelRenderable.builder()
              .setSource(context, Uri.parse("Cheetah.sfb"))
              .build();
      dog = ModelRenderable.builder()
              .setSource(context, Uri.parse("model_High Quality Scanned and cleaned dog_20170522_225430513.sfb"))
              .build();
      firetruck = ModelRenderable.builder()
              .setSource(context, Uri.parse("model.sfb"))
              .build();
      fish = ModelRenderable.builder()
              .setSource(context, Uri.parse("NOVELO_GOLDFISH.sfb"))
              .build();
      fox = ModelRenderable.builder()
              .setSource(context, Uri.parse("ArcticFox_Posed.sfb"))
              .build();
      militarytruck = ModelRenderable.builder()
              .setSource(context, Uri.parse("Tusk_Truck.sfb"))
              .build();
      policecar = ModelRenderable.builder()
              .setSource(context, Uri.parse("PoliceCar_1396.sfb"))
              .build();
      schoolbus = ModelRenderable.builder()
              .setSource(context, Uri.parse("Bus_1376.sfb"))
              .build();
      zebra = ModelRenderable.builder()
              .setSource(context, Uri.parse("Zebra.sfb"))
              .build();

      mapp.put("lion.jpg", lion);
      mapp.put("ambulance.png", ambulance);
      mapp.put("bulldozer.png", bulldozer);
      mapp.put("camel.png", camel);
      mapp.put("cat.png", cat);
      mapp.put("cheetah.png", cheetah);
      mapp.put("dog.jpg", dog);
      mapp.put("firetruck.png", firetruck);
      mapp.put("fish.jpg", fish);
      mapp.put("fox.png", fox);
      mapp.put("militarytruck.png", militarytruck);
      mapp.put("policecar.png", policecar);
      mapp.put("schoolbus.png", schoolbus);
      mapp.put("zebra.png", zebra);
    }


  }

  /**
   * Called when the AugmentedImage is detected and should be rendered. A Sceneform node tree is
   * created based on an Anchor created from the image. The corners are then positioned based on the
   * extents of the image. There is no need to worry about world coordinates since everything is
   * relative to the center of the image, which is the parent node of the corners.
   */
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
  public void setImage(AugmentedImage image) {
    this.image = image;

    CompletableFuture<ModelRenderable> img = mapp.get(image.getName());

//    // If any of the models are not loaded, then recurse when all are loaded.
//    if (!lion.isDone() ||!ulCorner.isDone() || !urCorner.isDone() || !llCorner.isDone() || !lrCorner.isDone()) {
//      CompletableFuture.allOf(lion, ulCorner, urCorner, llCorner, lrCorner)
//          .thenAccept((Void aVoid) -> setImage(image))
//          .exceptionally(
//              throwable -> {
//                Log.e(TAG, "Exception loading", throwable);
//                return null;
//              });
//    }

    if (!img.isDone()) {
      CompletableFuture.allOf(img)
              .thenAccept((Void aVoid) -> setImage(image))
              .exceptionally(
                      throwable -> {
                        Log.e(TAG, "Exception loading", throwable);
                        return null;
                      });
    }

    // Set the anchor based on the center of the image.
    setAnchor(image.createAnchor(image.getCenterPose()));

    // Make the 4 corner nodes.
    Vector3 localPosition = new Vector3();
    Node cornerNode;

    // Upper left corner.
    localPosition.set(-0.0f * image.getExtentX(), 0.0f, -0.0f * image.getExtentZ());
    cornerNode = new Node();
    cornerNode.setParent(this);
    cornerNode.setLocalPosition(localPosition);
    cornerNode.setLocalScale(new Vector3(0.1f, 0.1f, 0.1f));
    cornerNode.setLocalRotation(new Quaternion());
    cornerNode.setRenderable(img.getNow(null));

//    // Upper left corner.
//    localPosition.set(-0.5f * image.getExtentX(), 0.0f, -0.5f * image.getExtentZ());
//    cornerNode = new Node();
//    cornerNode.setParent(this);
//    cornerNode.setLocalPosition(localPosition);
//    cornerNode.setRenderable(ulCorner.getNow(null));
//
//    // Upper right corner.
//    localPosition.set(0.5f * image.getExtentX(), 0.0f, -0.5f * image.getExtentZ());
//    cornerNode = new Node();
//    cornerNode.setParent(this);
//    cornerNode.setLocalPosition(localPosition);
//    cornerNode.setRenderable(urCorner.getNow(null));
//
//    // Lower right corner.
//    localPosition.set(0.5f * image.getExtentX(), 0.0f, 0.5f * image.getExtentZ());
//    cornerNode = new Node();
//    cornerNode.setParent(this);
//    cornerNode.setLocalPosition(localPosition);
//    cornerNode.setRenderable(lrCorner.getNow(null));
//
//    // Lower left corner.
//    localPosition.set(-0.5f * image.getExtentX(), 0.0f, 0.5f * image.getExtentZ());
//    cornerNode = new Node();
//    cornerNode.setParent(this);
//    cornerNode.setLocalPosition(localPosition);
//    cornerNode.setRenderable(llCorner.getNow(null));
  }

  public AugmentedImage getImage() {
    return image;
  }
}
