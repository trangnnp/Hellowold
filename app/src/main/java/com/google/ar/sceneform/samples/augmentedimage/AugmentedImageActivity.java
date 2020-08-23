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

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.samples.common.helpers.SnackbarHelper;
import com.google.ar.sceneform.ux.ArFragment;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This application demonstrates using augmented images to place anchor nodes. app to include image
 * tracking functionality.
 */
public class AugmentedImageActivity extends AppCompatActivity {

  private ArFragment arFragment;
  private ImageView fitToScanView;
  private String cur = "";

  // Augmented image and its associated center pose anchor, keyed by the augmented image in
  // the database.
  private final Map<AugmentedImage, AugmentedImageNode> augmentedImageMap = new HashMap<>();
  private static final int MY_CAMERA_REQUEST_CODE = 100;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
    };

    arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
    fitToScanView = findViewById(R.id.image_view_fit_to_scan);

    arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);
    setupTopNav();

  }

  void setupTopNav(){
    View myView = findViewById(R.id.view_top);
    myView.setOnTouchListener(new View.OnTouchListener() {
      public boolean onTouch(View v, MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        String DEBUG_TAG = "MYTAG";
        switch(action) {
//          case (MotionEvent.ACTION_DOWN) :
//            Log.d(DEBUG_TAG,"Action was DOWN");
//            return true;
//          case (MotionEvent.ACTION_MOVE) :
//            Log.d(DEBUG_TAG,"Action was MOVE");
//            return true;
          case (MotionEvent.ACTION_UP) :
            myView.animate().translationY(myView.getY() - myView.getHeight()).setDuration(250).withEndAction(new Runnable() {
              @Override
              public void run() {
                myView.setY(myView.getY() - myView.getHeight());
              }
            });
            return true;
          default :
            return true;
        }
      }
    });

    View myView2 = findViewById(R.id.view_bottom);
    myView2.setAlpha(0);
    myView2.setOnTouchListener(new View.OnTouchListener() {
      public boolean onTouch(View v, MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        switch(action) {
          case (MotionEvent.ACTION_DOWN) :
            myView2.animate().alpha(0).setDuration(250).withEndAction(new Runnable() {
              @Override
              public void run() {
                myView2.setAlpha(0);
              }
            });
            return true;
          case (MotionEvent.ACTION_UP):
            myView2.animate().alpha(1).setDuration(250).withEndAction(new Runnable() {
              @Override
              public void run() {
                myView2.setAlpha(1);
              }
            });
            return true;
          default :
            return true;
        }
      }
    });

    View shareView = findViewById(R.id.view_share);
    shareView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        try {
          Intent shareIntent = new Intent(Intent.ACTION_SEND);
          shareIntent.setType("text/plain");
          shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Download backdrop for scanning");
          shareIntent.putExtra(Intent.EXTRA_TEXT, "https://drive.google.com/file/d/1sPpCtV_K5yqz_COmhFlu6v6oPWXj2wad/view?usp=sharing");
          startActivity(Intent.createChooser(shareIntent, "Download link using"));
        } catch(Exception e) {
          //e.toString();
          Toast.makeText(AugmentedImageActivity.this, "Share error", Toast.LENGTH_LONG).show();
        }
      }
    });
  }

  void dismissShare() {
    View myView = findViewById(R.id.view_share);
    myView.animate().translationX(myView.getX() - myView.getWidth()).setDuration(250).withEndAction(new Runnable() {
      @Override
      public void run() {
        myView.setX(myView.getX() - myView.getWidth());
      }
    });
  }

  void bringBackShare() {
    View myView = findViewById(R.id.view_share);
    myView.animate().translationX(0).setDuration(250).withEndAction(new Runnable() {
      @Override
      public void run() {
        myView.setX(0);
      }
    });
  }

  void dismissTopNav() {
    View myView = findViewById(R.id.view_top);
    myView.animate().translationY(myView.getY() - myView.getHeight()).setDuration(250).withEndAction(new Runnable() {
      @Override
      public void run() {
        myView.setY(myView.getY() - myView.getHeight());
      }
    });
  }

  void bringbackTopNav(String title, String subTitle){
    View myView = findViewById(R.id.view_top);
    myView.animate().translationY(0).setDuration(250).withEndAction(new Runnable() {
      @Override
      public void run() {
        myView.setY(0);
      }
    });

    TextView titleTxt = findViewById(R.id.text_top);
    TextView titleTxt2 = findViewById(R.id.text_top2);

    String subTi = "It's ";
    switch (subTitle) {
      case "lion.jpg":
        subTi += "a lion";
        break;
      case "ambulance.png":
        subTi += "an ambulance";
        break;
      case "bulldozer.png":
        subTi += "a bulldozer";
        break;
      case "camel.png":
        subTi += "a camel";
        break;
      case "cat.png":
        subTi += "a cat";
        break;
      case "cheetah.png":
        subTi += "a cheetah";
        break;
      case "dog.jpg":
        subTi += "a dog";
        break;
      case "firetruck.png":
        subTi += "a firetruck";
        break;
      case "fish.jpg":
        subTi += "a fish";
        break;
      case "fox.png":
        subTi += "a fox";
        break;
      case "militarytruck.png":
        subTi += "a military truck";
        break;
      case "policecar.png":
        subTi += "a police car";
        break;
      case "schoolbus.png":
        subTi += "a school bus";
        break;
      case "zebra.png":
        subTi += "a zebra";
        break;
      default:
        subTi = getString(R.string.subtitle_top_nav);
        break;
    }
    if (subTi == getString(R.string.subtitle_top_nav)) {
      titleTxt.setText(title);
      titleTxt2.setText(subTi);
    }
    else {
      titleTxt.setText(title + "\n" + subTi + ".");
      titleTxt2.setText("Hold camera still. We are rendering 3D object for you!");
    };

  }

  void dismissBottomBar() {
    View myView = findViewById(R.id.view_bottom);
    myView.animate().alpha(0).setDuration(250).withEndAction(new Runnable() {
      @Override
      public void run() {
        myView.setAlpha(0);
      }
    });
  }

  void bringbackBottomBar(String imgName) {
    View myView = findViewById(R.id.view_bottom);
    myView.animate().alpha(1).setDuration(250).withEndAction(new Runnable() {
      @Override
      public void run() {
        myView.setAlpha(1);
      }
    });

    TextView titleTxt = findViewById(R.id.text_bottom);
    TextView titleTxt2 = findViewById(R.id.text_bottom2);

    String subTi = "";
    String desc = "";
    switch (imgName) {
      case "lion.jpg":
        subTi += "A lion  /ˈlaɪ.ən/";
        desc += "The lion is a muscular, deep-chested cat with a short, rounded head, a reduced neck and round ears. Its fur varies in colour from light buff to silvery grey, yellowish red and dark brown. The colours of the underparts are generally lighter.";
        break;
      case "ambulance.png":
        subTi += "An ambulance  /ˈæm.bjə.ləns/";
        desc += "An ambulance is a medically equipped vehicle which transports patients to treatment facilities, such as hospitals. Typically, out-of-hospital medical care is provided to the patient. Ambulances are used to respond to medical emergencies by emergency medical services.";
        break;
      case "bulldozer.png":
        subTi += "A bulldozer  /ˈbʊlˌdoʊ.zɚ/";
        desc += "A bulldozer is a large and heavy tractor equipped with a substantial metal plate used to push large quantities of soil, sand, rubble, or similar material during construction or conversion work and typically equipped at the rear with a claw-like device to loosen densely compacted materials.";
        break;
      case "camel.png":
        subTi += "A camel  /ˈkæm.əl/";
        desc += "A camel is an even-toed ungulate in the genus Camelus that bears distinctive fatty deposits known as \"humps\" on its back. Camels have long been domesticated and, as livestock, they provide food and textiles.";
        break;
      case "cat.png":
        subTi += "A cat  /kæt/";
        desc += "The cat is a domestic species of small carnivorous mammal. It is the only domesticated species in the family Felidae and is often referred to as the domestic cat to distinguish it from the wild members of the family.";
        break;
      case "cheetah.png":
        subTi += "A cheetah  /ˈtʃiː.t̬ə/";
        desc += "The cheetah is a large cat native to Africa and central Iran. It is the fastest land animal, capable of running at 80 to 128 km/h, and as such has several adaptations for speed, including a light build, long thin legs and a long tail.";
        break;
      case "dog.jpg":
        subTi += "A dog  /dɑːɡ/";
        desc += "The dog is a domesticated carnivore of the family Canidae. It is part of the wolf-like canids, and is the most widely abundant terrestrial carnivore.";
        break;
      case "firetruck.png":
        subTi += "A fire truck  /ˈfaɪr ˌtrʌk/";
        desc += "A fire engine, also known in some places as a fire truck or fire lorry, is a road vehicle that functions as a firefighting apparatus. The primary purposes of a fire engine include transporting firefighters to an incident as well as carrying equipment for firefighting operations.";
        break;
      case "fish.jpg":
        subTi += "A fish  /fɪʃ/";
        desc += "Fish are gill-bearing aquatic craniate animals that lack limbs with digits. They form a sister group to the tunicates, together forming the olfactores. Included in this definition are the living hagfish, lampreys, and cartilaginous and bony fish as well as various extinct related groups.";
        break;
      case "fox.png":
        subTi += "A fox  /fɑːks/";
        desc += "Foxes are small to medium-sized, omnivorous mammals belonging to several genera of the family Canidae. Foxes have a flattened skull, upright triangular ears, a pointed, slightly upturned snout, and a long bushy tail. Twelve species belong to the monophyletic \"true foxes\" group of genus Vulpes.";
        break;
      case "militarytruck.png":
        subTi += "A military truck  /ˈmɪl.ə.ter.i, trʌk/";
        desc += "A military vehicle is a type of vehicle that includes all land combat and transportation vehicles, which are designed for or are significantly used by military forces. Many military vehicles have vehicle armour plate or off-road capabilities or both.";
        break;
      case "policecar.png":
        subTi += "A police car  /pəˈliːs ˌkɑːr/";
        desc += "A police car is a ground vehicle used by police for transportation during patrols and to enable them to respond to incidents and chases. ";
        break;
      case "schoolbus.png":
        subTi += "A school bus  /ˈskuːl ˌbʌs/";
        desc += "A school bus is a type of bus owned, leased, contracted to, or operated by a school or school district. It is regularly used to transport students to and from school or school-related activities, but not including a charter bus or transit bus.";
        break;
      case "zebra.png":
        subTi += "A zebra  /ˈziː.brə/";
        desc += "Zebras are African equines with distinctive black-and-white striped coats. There are three extant species: the Grévy's zebra, plains zebra and the mountain zebra. Their stripes come in different patterns, unique to each individual.";
        break;
      default:
        break;
    }

    titleTxt.setText(subTi);
    titleTxt2.setText(desc);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == MY_CAMERA_REQUEST_CODE) {
      if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
      } else {
        Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
      }
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (augmentedImageMap.isEmpty()) {
      fitToScanView.setVisibility(View.VISIBLE);
      bringbackTopNav("HelloWorld app", "none");
      dismissBottomBar();
      bringBackShare();
    }
  }

  /**
   * Registered with the Sceneform Scene object, this method is called at the start of each frame.
   *
   * @param frameTime - time since last frame.
   */
  private void onUpdateFrame(FrameTime frameTime) {
    Frame frame = arFragment.getArSceneView().getArFrame();

    // If there is no frame or ARCore is not tracking yet, just return.
    if (frame == null || frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
      return;
    }

    Collection<AugmentedImage> updatedAugmentedImages =
        frame.getUpdatedTrackables(AugmentedImage.class);
    for (AugmentedImage augmentedImage : updatedAugmentedImages) {
      dismissShare();
      switch (augmentedImage.getTrackingState()) {
        case PAUSED:
          // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
          // but not yet tracked.
//          String text = "Detected Image " + augmentedImage.getIndex();
//          SnackbarHelper.getInstance().showMessage(this, augmentedImage.getName());
          bringbackTopNav("Object found!", augmentedImage.getName());
          break;

        case TRACKING:
          // Have to switch to UI Thread to update View.
          fitToScanView.setVisibility(View.GONE);
          dismissTopNav();
          bringbackBottomBar(augmentedImage.getName());
          // Create a new anchor for newly found images.
          if (!augmentedImageMap.containsKey(augmentedImage)) {
            AugmentedImageNode node = new AugmentedImageNode(this);
            node.setImage(augmentedImage);
            augmentedImageMap.put(augmentedImage, node);
            arFragment.getArSceneView().getScene().addChild(node);
          }
          break;

        case STOPPED:
          augmentedImageMap.remove(augmentedImage);
          break;
      }
    }
  }

}
