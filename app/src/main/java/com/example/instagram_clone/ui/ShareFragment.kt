package com.example.instagram_clone.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.instagram_clone.R
import com.example.instagram_clone.utils.CameraHelper
import kotlinx.android.synthetic.main.fragment_share.*


class ShareFragment : Fragment() {

    private lateinit var mCameraHelper: CameraHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_share, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mCameraHelper = CameraHelper(requireActivity())
        mCameraHelper.takeCameraPicture()

        back_image.setOnClickListener{ }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == mCameraHelper.REQUEST_CODE && resultCode == RESULT_OK) {
            Glide.with(requireActivity()).load(mCameraHelper.imageUri).centerCrop().into(post_image)
        }
    }

}