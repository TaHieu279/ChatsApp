package com.tavanhieu.chatapp.fragment_dang_nhap_dang_ky

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.tavanhieu.chatapp.R
import com.tavanhieu.chatapp.m_class.HangSo
import com.tavanhieu.chatapp.m_class.User


class FragmentDangKy: Fragment() {
    private lateinit var mView: View
    private lateinit var edtFullName: EditText
    private lateinit var edtAccount: EditText
    private lateinit var edtPassWord: EditText
    private lateinit var edtPassWord2: EditText
    private lateinit var txtDangNhap: TextView
    private lateinit var btnDangKy: Button
    private lateinit var mAuth: FirebaseAuth
    private var db = Firebase.database

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mView = inflater.inflate(R.layout.dang_ky, container, false)
        anhXa()
        mAuth = FirebaseAuth.getInstance()
        mOnClick()
        return mView
    }

    private fun anhXa() {
        edtFullName     = mView.findViewById(R.id.txtInputEdtFullNameDangKy)
        edtAccount      = mView.findViewById(R.id.txtInputEdtAccountDangKy)
        edtPassWord     = mView.findViewById(R.id.txtInputEdtPassWordDangKy)
        edtPassWord2    = mView.findViewById(R.id.txtInputEdtPassWord2DangKy)
        txtDangNhap     = mView.findViewById(R.id.txtDangNhapInDangKy)
        btnDangKy       = mView.findViewById(R.id.btnSignUpDangKy)
    }

    private fun mOnClick() {
        val supportFragment = requireActivity().supportFragmentManager.beginTransaction()

        btnDangKy.setOnClickListener {
            signUp(supportFragment)
        }

        txtDangNhap.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack("DangNhap", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            supportFragment.replace(R.id.fragmentDangNhapDangKy, FragmentDangNhap()).commit()
        }
    }

    private fun signUp(supportFragment: FragmentTransaction) {
        val hoTen: String = edtFullName.text.trim().toString()
        val email: String = edtAccount.text.trim().toString()
        val pass: String  = edtPassWord.text.trim().toString()
        val pass2: String = edtPassWord2.text.trim().toString()

        //Ki???m tra null:
        if(hoTen.isEmpty() || hoTen == "") {
            edtFullName.error = "Kh??ng ???????c ????? tr???ng"
            edtFullName.requestFocus()
        } else if(email.isEmpty() || email == "") {
            edtAccount.error = "Kh??ng ???????c ????? tr???ng"
            edtAccount.requestFocus()
        } else if(!email.matches(HangSo.REGEX_EMAIL.toRegex())) {
            //Ki???m tra ?????nh d???ng email.
            edtAccount.error = "Email kh??ng h???p l???"
            edtAccount.requestFocus()
        } else if(pass.isEmpty() || pass == "") {
            edtPassWord.error = "Kh??ng ???????c ????? tr???ng"
            edtPassWord.requestFocus()
        } else if(pass.length < 6) {
            edtPassWord.error = "M???t kh???u ??t nh???t 6 k?? t???"
            edtPassWord.requestFocus()
        } else if(pass2.isEmpty() || pass2 == "") {
            edtPassWord2.error = "Kh??ng ???????c ????? tr???ng"
            edtPassWord2.requestFocus()
        } else if(pass != pass2) {
            edtPassWord2.error = "M???t kh???u kh??ng kh???p"
            edtPassWord2.requestFocus()
        } else {
            signUp(email, pass, hoTen, supportFragment)
        }
    }

    private fun signUp(email: String, pass: String, hoTen: String, supportFragment: FragmentTransaction) {
        mAuth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    //Th??m d??? li???u v??o realTime DB:
                    db.reference.child(HangSo.KEY_USER)
                        .child(mAuth.currentUser?.uid!!)
                        .setValue(User(email, hoTen, mAuth.currentUser?.uid!!, 0, "", "", null, null))
                    //????ng xu???t ng?????i d??ng ????? nh???p m???t kh???u ??? ????ng nh???p:
                    mAuth.signOut()

                    //Chuy???n v??? m??n h??nh ????ng nh???p:
                    Toast.makeText(requireContext(), "????ng k?? th??nh c??ng", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack("DangNhap", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    supportFragment.replace(R.id.fragmentDangNhapDangKy, FragmentDangNhap()).commit()
                } else {
                    Toast.makeText(requireContext(), "????ng k?? kh??ng th??nh c??ng", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
