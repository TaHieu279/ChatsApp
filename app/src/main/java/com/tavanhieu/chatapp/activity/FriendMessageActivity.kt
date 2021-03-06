package com.tavanhieu.chatapp.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.tavanhieu.chatapp.R
import com.tavanhieu.chatapp.adpater.AdapterListMessage
import com.tavanhieu.chatapp.fcm_notifications.MyFirebaseMessagingSend
import com.tavanhieu.chatapp.m_class.Conversations
import com.tavanhieu.chatapp.m_class.HangSo
import com.tavanhieu.chatapp.m_class.Message
import com.tavanhieu.chatapp.m_class.User
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception
import java.lang.NullPointerException
import java.util.*
import kotlin.collections.ArrayList

class FriendMessageActivity : UserActiveActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var imgBack: ImageView
    private lateinit var imgFriend: CircleImageView
    private lateinit var txtFriend: TextView
    private lateinit var imgCall: ImageView
    private lateinit var imgVideo: ImageView
    private lateinit var imgThongTin: ImageView
    private lateinit var cameraBottom: ImageView
    private lateinit var pictureBottom: ImageView
    private lateinit var edtMessBottom: EditText
    private lateinit var sendMessBottom: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var rcvListMessage: RecyclerView
    private lateinit var imgUserInfoMessage: CircleImageView
    private lateinit var userNameInfoMessage: TextView
    private lateinit var buttonInfoInfoMessage: ImageView
    private lateinit var buttonXoaInfoMessage: ImageView

    private lateinit var uidReceiver: String
    private lateinit var hoTenReceiver: String
    private lateinit var uidSender: String
    private lateinit var mAdapter: AdapterListMessage
    private var arr = ArrayList<Message>()

    private lateinit var nguoiNhan: User
    private lateinit var nguoiGui: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_message)
        anhXa()
        try {
            //Nh???n info t??? m??n h??nh homeChat:
            hoTenReceiver = intent.getStringExtra("hoTen").toString()
            //L???y id c???a ng?????i g???i v?? ng?????i nh???n:
            uidReceiver = intent.getStringExtra("receiverId")!!
            uidSender   = FirebaseAuth.getInstance().currentUser?.uid!!

            //L???y ra Ng?????i g???i/ Ng?????i nh???n hi???n t???i: (Ch??a hi???u qu?? tr??nh load c???a Firebase cho l???m...)
            Firebase.database.reference.child(HangSo.KEY_USER)
                .addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(data in snapshot.children) {
                            val user = data.getValue(User::class.java)!!
                            if(user.uid.equals(uidSender))
                                nguoiGui = user

                            if(user.uid.equals(uidReceiver)) {
                                nguoiNhan = user
                                //Load ???nh:
                                if(nguoiNhan.anh != null) {
                                    Picasso.get().load(nguoiNhan.anh).into(imgFriend)
                                    Picasso.get().load(nguoiNhan.anh).into(imgUserInfoMessage)
                                }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })

            //G??n t??n l??n toolbar, ?????c message:
            txtFriend.text = hoTenReceiver
            userNameInfoMessage.text = hoTenReceiver
            docMessage()
            //??nh x??? adapter cho message
            mAdapter = AdapterListMessage(this)
            mAdapter.setData(arr)
            rcvListMessage.adapter = mAdapter

            mOnClick()
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun anhXa() {
        imgBack     = findViewById(R.id.imgArrowBackToolBarMessage)
        imgFriend   = findViewById(R.id.imgIconFriendToolbarMessage)
        txtFriend   = findViewById(R.id.txtNameFriendToolbarMessage)
        imgCall     = findViewById(R.id.imgCallActionBarMessage)
        imgVideo    = findViewById(R.id.imgVideoActionBarMessage)
        imgThongTin = findViewById(R.id.imgInformationActionBarMessage)
        cameraBottom   = findViewById(R.id.cameraMenuBottomMessage)
        pictureBottom  = findViewById(R.id.pictureMenuBottomMessage)
        edtMessBottom  = findViewById(R.id.inputMessMenuBottonMessage)
        sendMessBottom = findViewById(R.id.sendMenuBottomMessage)
        progressBar    = findViewById(R.id.progessBarFriendMessage)
        rcvListMessage = findViewById(R.id.rcvListMessFriendMessage)
        drawerLayout   = findViewById(R.id.drawerThongTin_FriendMessage)
        imgUserInfoMessage      = findViewById(R.id.imgUser_InfoMessage)
        userNameInfoMessage     = findViewById(R.id.txtUserName_InfoMessage)
        buttonInfoInfoMessage   = findViewById(R.id.imgButtonInfo_InfoMessage)
        buttonXoaInfoMessage    = findViewById(R.id.imgButtonXoa_InfoMessage)
    }

    private fun mOnClick() {
        imgBack.setOnClickListener { onBackPressed() }
        sendMessBottom.setOnClickListener { sendMessage() }
        imgThongTin.setOnClickListener { drawerLayout.openDrawer(GravityCompat.END) }
        buttonXoaInfoMessage.setOnClickListener {
            AlertDialog.Builder(this)
                .setIcon(R.drawable.logo_aph)
                .setTitle("Th??ng b??o")
                .setMessage("B???n c?? mu???n x??a cu???c tr?? chuy???n n??y?")
                .setPositiveButton("C??", MyListener(this, uidReceiver, uidSender))
                .setNegativeButton("Kh??ng", MyListener(this, uidReceiver, uidSender)).show()
        }
    }
    private fun sendMessage() {
        val mess = edtMessBottom.text.trim().toString()
        val date = Date()

        //Ki???m tra n???u tin nh???n kh??ng r???ng th?? cho ph??p th???c hi???n:
        if(mess.isNotEmpty() || mess != "") {
            //Add tin nh???n cho ng?????i g???i:
            Firebase.database.reference.child(HangSo.KEY_CHATS_TTCN)
                .child(uidSender+uidReceiver)
                .child(HangSo.KEY_MESSAGE).push()
                .setValue(Message(date, mess, uidSender, null))
                .addOnSuccessListener {

                    //Add tin nh???n cho ng?????i nh???n:
                    Firebase.database.reference.child(HangSo.KEY_CHATS_TTCN)
                        .child(uidReceiver+uidSender)
                        .child(HangSo.KEY_MESSAGE).push()
                        .setValue(Message(date, mess, uidSender, null))

                    //Add ng?????i ???? nh???n:
                    Firebase.database.reference.child(HangSo.KEY_CONVERSATIONS)
                        .child(uidSender)
                        .child(uidReceiver)
                        .setValue(Conversations(nguoiNhan.hoTen!!, "B???n: $mess", date, uidReceiver, nguoiNhan.anh))

                    //Add ng?????i nh???n cho ng?????i nh???n:
                    Firebase.database.reference.child(HangSo.KEY_CONVERSATIONS)
                        .child(uidReceiver)
                        .child(uidSender)
                        .setValue(Conversations(nguoiGui.hoTen!!, mess, date, uidSender, nguoiGui.anh))
                }
            //G???i th??ng b??o:
            try {
                MyFirebaseMessagingSend.pushNotifications(this, nguoiNhan.token!!, "${nguoiGui.hoTen!!} g???i t???i ${nguoiNhan.hoTen!!}", mess)
            } catch (ex: NullPointerException) {}
        }
        //Sau khi g???i xong x??a n???i dung nh???n tr?????c ????:
        edtMessBottom.text = null
    }

    private fun docMessage() {
        progressBar.visibility = View.VISIBLE

        //?????c message t??? firebase v?? g??n v??o m???ng:
        Firebase.database.reference.child(HangSo.KEY_CHATS_TTCN)
            .child(uidSender+uidReceiver)
            .child(HangSo.KEY_MESSAGE)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    arr.clear()
                    //Th??m d??? li???u tin nh???n v??o m???ng:
                    for(data in snapshot.children) {
                        val mess = data.getValue(Message::class.java)
                        arr.add(mess!!)
                    }
                    mAdapter.notifyDataSetChanged()
                    rcvListMessage.scrollToPosition(arr.size - 1)
                    progressBar.visibility = View.GONE
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}

class MyListener(var context: Context, var uidReceiver: String, var uidSender: String) : DialogInterface.OnClickListener {
    override fun onClick(dialog: DialogInterface?, which: Int) {
        if(which == -1) {
            //Ch??? x??a tin nh???n t???i uid c???a ng?????i g???i, ??o???n chat c???a ng?????i nh???n s??? ko b??? thay ?????i...
            //X??a Conversation:
            Firebase.database.reference.child(HangSo.KEY_CONVERSATIONS)
                .child(uidSender)
                .child(uidReceiver)
                .removeValue()
            //X??a message:
            Firebase.database.reference.child(HangSo.KEY_CHATS_TTCN)
                .child(uidSender+uidReceiver)
                .child(HangSo.KEY_MESSAGE)
                .removeValue()
            //Hi???n th??? th??ng b??o:
            Toast.makeText(context, "???? x??a", Toast.LENGTH_SHORT).show()
            (context as FriendMessageActivity).finish()
        }
    }
}