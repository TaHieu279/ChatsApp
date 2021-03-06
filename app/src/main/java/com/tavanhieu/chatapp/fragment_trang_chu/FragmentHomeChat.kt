package com.tavanhieu.chatapp.fragment_trang_chu

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.tavanhieu.chatapp.R
import com.tavanhieu.chatapp.adpater.AdapterContactChat
import com.tavanhieu.chatapp.adpater.AdapterListChatMain
import com.tavanhieu.chatapp.adpater.AdapterUserActive
import com.tavanhieu.chatapp.m_class.Conversations
import com.tavanhieu.chatapp.m_class.HangSo
import com.tavanhieu.chatapp.m_class.User

class FragmentHomeChat : Fragment() {
    private lateinit var mView: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var rcvUserActive: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var mSearchView: SearchView
    private lateinit var mAdapterConversation: AdapterListChatMain
    private lateinit var mAdapterUserActive: AdapterUserActive
    private var arr: ArrayList<Conversations>  = ArrayList()
    private var arrSearch: ArrayList<User>     = ArrayList()
    private var arrUserActive: ArrayList<User> = ArrayList()
    private val db = Firebase.database.reference.child(HangSo.KEY_USER)
    private val uid = FirebaseAuth.getInstance().currentUser?.uid!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mView = inflater.inflate(R.layout.activity_home_chat, container, false)
        anhXa()
        docUserDaNhanTin()
        docUserActive()

        //??nh x??? adapter list conversation
        mAdapterConversation = AdapterListChatMain(requireContext())
        mAdapterConversation.setData(arr)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        recyclerView.adapter = mAdapterConversation

        //??nh x??? adapter list user active
        mAdapterUserActive = AdapterUserActive(requireContext())
        mAdapterUserActive.setData(arrUserActive)
        rcvUserActive.adapter = mAdapterUserActive

        //Back Pressed: (????ng search n???u ??ang m???) //Kh??ng ho???t ?????ng...
//            activity?.onBackPressedDispatcher?.addCallback(requireActivity(), object: OnBackPressedCallback(true) {
//                override fun handleOnBackPressed() {
//                    //????ng search n???u ??ang m???:
//                    if (!mSearchView.isIconified) {
//                        mSearchView.isIconified = true
//                        mSearchView.onActionViewCollapsed()
//                        recyclerView.adapter = mAdapterConversation
//                    } else
//                        activity?.onBackPressed()
//                }
//            })
        mOnClick()
        return mView
    }

    private fun mOnClick() {
        mSearchView.setOnQueryTextFocusChangeListener(object: View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, p1: Boolean) {
                mSearchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }
                    override fun onQueryTextChange(newText: String?): Boolean {
                        //Hi???n th??? list search:
                        db.addValueEventListener(object: ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    arrSearch.clear()
                                    //L???y danh s??ch search:
                                    for(data in snapshot.children) {
                                        val user = data.getValue(User::class.java)
                                        if(newText != "" && user!!.hoTen!!.contains(newText.toString(), true))
                                            arrSearch.add(user)
                                    }
                                    //??nh x??? view adapter:
                                    try {
                                        val mAdapter2 = AdapterContactChat(requireContext())
                                        mAdapter2.setData(arrSearch)
                                        recyclerView.adapter = mAdapter2
                                    } catch (e: Exception) {}
                                }
                                override fun onCancelled(error: DatabaseError) {}
                            })
                        return false
                    }
                })
            }
        })
        //M??? l???i list message ???? nh???n:
        mSearchView.setOnCloseListener {
            recyclerView.adapter = mAdapterConversation
            false
        }
    }

    private fun anhXa() {
        recyclerView  = mView.findViewById(R.id.recycleViewListChatMain)
        progressBar   = mView.findViewById(R.id.progressBarListChatMain)
        mSearchView   = mView.findViewById(R.id.searchViewListChatMain)
        rcvUserActive = mView.findViewById(R.id.recycleViewUserActiveChatMain)
    }

    private fun docUserDaNhanTin() {
        //?????c tin nh???n ???? g???i:
        progressBar.visibility = View.VISIBLE
        Firebase.database.reference.child(HangSo.KEY_CONVERSATIONS)
            .child(uid)
            .addValueEventListener(object: ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                arr.clear()
                for (data in snapshot.children) {
                    val userConversations = data.getValue(Conversations::class.java)
                    arr.add(userConversations!!)
                }
                mAdapterConversation.notifyDataSetChanged()
                arr.sortByDescending { item -> item.thoiGianGui }
                progressBar.visibility = View.GONE
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun docUserActive() {
        //?????c ng?????i d??ng ??ang ho???t ?????ng:
        db.addValueEventListener(object: ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    arrUserActive.clear()
                    for(data in snapshot.children) {
                        val user = data.getValue(User::class.java)
                        if(user?.trangThaiHoatDong == 1 && user.uid != uid) {
                            arrUserActive.add(user)
                        }
                    }
                    arrUserActive.sortBy { item -> item.hoTen }
                    mAdapterUserActive.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}