package com.example.findyourselfinthephoto.Fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.findyourselfinthephoto.R;


public class Home extends Fragment {

    private ImageView upload_button;
    private Uri ImageUri;
    private static final int GALLERYPICK = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        upload_button = view.findViewById(R.id.upload_button);

        upload_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenGallery();
            }
        });

        return view;
    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERYPICK);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERYPICK && resultCode == RESULT_OK && data != null){
            ImageUri = data.getData();
            upload_button.setImageURI(ImageUri);
        }
    }
//private void showFileChooser() {
//Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//intent.setType("*/*");
/*intent.addCategory(Intent.CATEGORY_OPENABLE);
Intent chooser = Intent.createChooser(intent, "Select a file to Upload");

try {
startActivityForResult(chooser, 0);
} catch (android.content.ActivityNotFoundException ex) {
Toast.makeText(getActivity(), "Install a File Manager!", Toast.LENGTH_SHORT).show();
}*/

//}

}