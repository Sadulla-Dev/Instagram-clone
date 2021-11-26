package com.example.instagram_clone.activities


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagram_clone.R
import com.example.instagram_clone.models.User
import com.example.instagram_clone.ui.loadUserPhoto
import com.example.instagram_clone.utils.FirebaseHelper
import com.example.instagram_clone.utils.ValueEventListenerAdapter
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile.profile_image

class ProfileActivity : BaseActivity(4) {
    private lateinit var mFirebase: FirebaseHelper
    private lateinit var mUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setupBottomNavigation()

        edit_profile_btn.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        settings_image.setOnClickListener {
            startActivity(Intent(this, ProfileSettingActivity::class.java))
        }

        add_friends_image.setOnClickListener { startActivity(Intent(this, AddFriendsActivity::class.java)) }

        mFirebase = FirebaseHelper(this)
        mFirebase.currentUserReference().addValueEventListener(ValueEventListenerAdapter{

            mUser = it.getValue(User::class.java)!!
            profile_image.loadUserPhoto(mUser.photo)
            username_text.text = mUser.name
            posts_count_text.text = mUser.phone.toString()
            followers_count_text.text = mUser.phone.toString()
        })


        images_recycler.layoutManager = GridLayoutManager(this, 3)
        mFirebase.database.child("images").child(mFirebase.auth.currentUser!!.uid)
            .addValueEventListener(ValueEventListenerAdapter {
                val images = it.children.map { it.getValue(String::class.java)!! }
                images_recycler.adapter = ImagesAdapter(images)
            })
    }
}

class ImagesAdapter(private val images: List<String>): RecyclerView.Adapter<ImagesAdapter.ViewHolder>(){
    class ViewHolder(val image:ImageView):RecyclerView.ViewHolder(image)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val image = LayoutInflater.from(parent.context).inflate(R.layout.image_item,parent,false) as ImageView
        return ViewHolder(image)
    }

    override fun getItemCount(): Int = images.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.image.loadImage(images[position])
    }
    private fun ImageView.loadImage(image:String){
        Glide.with(this).load(image).centerCrop().into(this)
    }
}
class SquareImageView(context: Context,attrs:AttributeSet):androidx.appcompat.widget.AppCompatImageView(context,attrs){
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}