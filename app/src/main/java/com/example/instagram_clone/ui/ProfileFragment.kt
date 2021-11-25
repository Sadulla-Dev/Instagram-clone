package com.example.instagram_clone.ui


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.instagram_clone.R
import com.example.instagram_clone.activities.EditProfileActivity
import com.example.instagram_clone.utils.FirebaseHelper
import com.example.instagram_clone.models.User
import com.example.instagram_clone.utils.ValueEventListenerAdapter
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {
    private lateinit var mFirebaseHelper: FirebaseHelper
    private lateinit var mUser: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_profile, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        edit_profile_btn.setOnClickListener {
//            Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_editProfileFragment)
            requireActivity().run {
                startActivity(Intent(this, EditProfileActivity::class.java))
            }
        }


        mFirebaseHelper = FirebaseHelper(requireActivity())
        mFirebaseHelper.currentUserReference().addValueEventListener(ValueEventListenerAdapter{
            mUser = it.getValue(User::class.java)!!
            Glide.with(requireActivity()).load(mUser.photo).into(profile_image)
            username_text.text = mUser.name
            posts_count_text.text = mUser.phone.toString()
            followers_count_text.text = mUser.phone.toString()
        })
    }
}