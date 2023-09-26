package com.lovejeet.firebase

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Note
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lovejeet.firebase.databinding.ActivityMainBinding
import com.lovejeet.firebase.databinding.CustomdialogBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity(), ListInterface {
    lateinit var binding: ActivityMainBinding
    lateinit var recyclerAdapter: RecyclerAdapter
    lateinit var layoutManager: LinearLayoutManager
    var firestore= Firebase.firestore
    var notes:ArrayList<Notes> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerAdapter= RecyclerAdapter(notes,this)
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvNotes.layoutManager = layoutManager
        binding.rvNotes.adapter = recyclerAdapter

        getCollectionData()

        binding.fabAdd.setOnClickListener {
            var dialog=Dialog(this)
            var dialogBinding:CustomdialogBinding
            dialogBinding= CustomdialogBinding.inflate(layoutInflater)
            dialog.setContentView(dialogBinding.root)
            //   throw RuntimeException("Test Crash")
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.show()
            dialogBinding.textClock.setOnClickListener {
                Toast.makeText(this@MainActivity, "TextClock", Toast.LENGTH_SHORT).show()
            }
            dialogBinding.btnAdd.setOnClickListener {
                if (dialogBinding.etTitle.text?.isEmpty() == true) {
                    dialogBinding.etTitle.error = "Enter Title"
                } else if (dialogBinding.etDescription.text?.isEmpty() == true) {
                    dialogBinding.etDescription.error = "Enter Description"
                } else {
                    dialogBinding.textClock.visibility = View.VISIBLE
                    firestore.collection("Users")
                        .add(
                            Notes(
                                title = dialogBinding.etTitle.text.toString(),
                                description = dialogBinding.etDescription.text.toString(),
                                time = dialogBinding.textClock.text.toString()
                            )
                        )
                        .addOnSuccessListener {
                            Toast.makeText(this, "Data Added", Toast.LENGTH_SHORT).show()
                            getCollectionData()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Data Addition Failed", Toast.LENGTH_SHORT).show()
                        }
                        .addOnCanceledListener {
                            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
                        }

                    dialog.dismiss()
                }
            }
        }
    }

    private fun getCollectionData() {
        notes.clear()
        firestore.collection("Users")
            .get()
            .addOnSuccessListener {
                //  System.out.println("in snapshot ${it.documents}")
                for(items in it.documents){
                    var firestoreClass= items.toObject(Notes::class.java)?:Notes()
                    firestoreClass.id=items.id
                    notes.add(firestoreClass)
                }
                recyclerAdapter.notifyDataSetChanged()
            }
    }


    override fun onDeleteClick(notes: Notes, position: Int) {
        AlertDialog.Builder(this)
            .setTitle(this.resources.getString(R.string.delete))
            .setMessage(this.resources.getString(R.string.delete_msg))
            .setPositiveButton(this.resources.getString(R.string.yes)) { _, _ ->
                firestore.collection("Users").document(notes.id?:"").delete()
                    .addOnSuccessListener {
                    Toast.makeText(this, "Data Deleted", Toast.LENGTH_SHORT).show()
                    getCollectionData()
                }
                    .addOnFailureListener {
                        Toast.makeText(this, "Data Deletion Failed", Toast.LENGTH_SHORT).show()
                    }
                    .addOnCanceledListener {
                        Toast.makeText(this, "Data Deletion Cancelled", Toast.LENGTH_SHORT).show()
                    }

            }
            .setNegativeButton(this.resources.getString(R.string.no)){ _, _->

            }
            .show()
    }

    override fun onUpdateClick(notes: Notes, position: Int) {
        var dialog=Dialog(this)
        var dialogBinding : CustomdialogBinding
        dialogBinding= CustomdialogBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialogBinding.btnAdd.text = "Update"
        dialogBinding.textClock.visibility = View.GONE
        dialogBinding.etTitle.setText(notes.title)
        dialogBinding.etDescription.setText(notes.description)
        dialog.show()

        dialogBinding.btnAdd.setOnClickListener {
            if (dialogBinding.etTitle.text?.isEmpty() == true) {
                dialogBinding.etTitle.error = "Enter Title"
            } else if (dialogBinding.etDescription.text?.isEmpty() == true) {
                dialogBinding.etDescription.error = "Enter Description"
            } else {
                var updateNotes = Notes(
                    title = dialogBinding.etTitle.text.toString(),
                    description = dialogBinding.etDescription.text.toString(),
                    time = dialogBinding.textClock.text.toString(),
                    id = notes.id ?: ""
                )
                firestore.collection("Users")
                    .document(notes.id ?: "")
                    .set(updateNotes)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Data Updated", Toast.LENGTH_SHORT).show()
                        getCollectionData()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Data Updation Failed", Toast.LENGTH_SHORT).show()
                    }
                    .addOnCanceledListener {
                        Toast.makeText(this, "Data Updation Cancelled", Toast.LENGTH_SHORT).show()
                    }
                dialog.dismiss()
            }
        }
    }}