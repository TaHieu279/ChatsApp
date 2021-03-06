package com.tavanhieu.chatapp.fragment_dang_nhap_dang_ky

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.tavanhieu.chatapp.activity.MainActivity
import com.tavanhieu.chatapp.R
import com.tavanhieu.chatapp.m_class.HangSo

class FragmentDangNhap: Fragment() {
    private lateinit var mView: View
    private lateinit var edtAccount: EditText
    private lateinit var edtPassWord: EditText
    private lateinit var cbNhoMatKhau: CheckBox
    private lateinit var txtQuenMatKhau: TextView
    private lateinit var txtTaoTaiKhoan: TextView
    private lateinit var btnDangNhap: Button
    private lateinit var imgLoginFaceBook: ImageView
    private lateinit var imgLoginGoogle: ImageView
    private lateinit var mShared: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mView = inflater.inflate(R.layout.dang_nhapp, container, false)
        anhXa()

        mShared = container!!.context.getSharedPreferences("MY_SHARED", Context.MODE_PRIVATE)
        //Tự động đăng nhập nếu đã đăng nhập trước đó:
        val authCurrent = FirebaseAuth.getInstance().currentUser
        if (authCurrent != null) {
            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish()
        }
        //Load tài khoản/ mật khẩu đã lưu:
        edtAccount.setText(mShared.getString("taiKhoan", null))
        edtPassWord.setText(mShared.getString("matKhau", null))
        if (edtAccount.text != null && !edtAccount.text.equals("")) {
            cbNhoMatKhau.isChecked = true
        }
        mOnClick()
        return mView
    }

    private fun anhXa() {
        edtAccount      = mView.findViewById(R.id.txtInputEdtAccountDangNhap)
        edtPassWord     = mView.findViewById(R.id.txtInputEdtPassWord)
        cbNhoMatKhau    = mView.findViewById(R.id.checkRememberAccount)
        txtQuenMatKhau  = mView.findViewById(R.id.txtForgetPasswordDangNhap)
        txtTaoTaiKhoan  = mView.findViewById(R.id.txtNewAccountDangNhap)
        btnDangNhap     = mView.findViewById(R.id.btnSignInDangNhap)
        imgLoginFaceBook = mView.findViewById(R.id.imgLoginFacebookDangNhap)
        imgLoginGoogle   = mView.findViewById(R.id.imgLoginGoogleDangNhap)
    }

    private fun mOnClick() {
        val supportFragment = requireActivity().supportFragmentManager.beginTransaction()
        btnDangNhap.setOnClickListener {
            val account  = edtAccount.text.trim().toString()
            val password = edtPassWord.text.trim().toString()
            if(account.isEmpty() || account == "") {
                edtAccount.error = "Không được để trống"
            } else if(password.isEmpty() || password == "") {
                edtPassWord.error = "Không được để trống"
            } else {
                login(account, password)
            }
        }

        txtQuenMatKhau.setOnClickListener {
            supportFragment.replace(R.id.fragmentDangNhapDangKy, FragmentXacMinhMatKhau())
                .addToBackStack("DangNhap").commit()
        }

        txtTaoTaiKhoan.setOnClickListener {
            supportFragment.replace(R.id.fragmentDangNhapDangKy, FragmentDangKy())
                .addToBackStack("DangNhap").commit()
        }
    }

    @SuppressLint("CommitPrefEdits")
    private fun login(account: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(account, password)
            .addOnCompleteListener(requireActivity()) {task ->
                if(task.isSuccessful) {
                    //lưu tài khoản vào data local:
                    if(cbNhoMatKhau.isChecked) {
                        mShared.edit().putString("taiKhoan", account)?.apply()
                        mShared.edit().putString("matKhau", password)?.apply()
                    } else {
                        mShared.edit().clear()?.apply()
                    }
                    //Gán token cho thiết bị đã cài app:
                    FirebaseMessaging.getInstance().token.addOnCompleteListener {
                        if(it.isSuccessful) {
                            Firebase.database.reference.child(HangSo.KEY_USER)
                                .child(FirebaseAuth.getInstance().currentUser?.uid!!)
                                .child("token")
                                .setValue(it.result)
                        }
                    }
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finish()
                } else {
                    Toast.makeText(requireContext(), "Tài khoản không chính xác", Toast.LENGTH_SHORT).show()
                }
            }
    }
}