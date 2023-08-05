package com.xiaeer.lock.ui.fragment

import android.app.Dialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import com.drake.tooltip.toast
import com.xiaeer.lock.data.room.model.LockTask
import com.xiaeer.lock.databinding.FragmentEditLockTaskBinding
import com.xiaeer.lock.ui.activity.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class EditLockTaskFragment : Fragment() {

    private var _binding: FragmentEditLockTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var lockTask: LockTask

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditLockTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController(it).navigateUp()
        }

        binding.btnSave.setOnClickListener {
            if (lockTask.startTime == null) {
                toast("请选择开始时间")
                return@setOnClickListener
            }
            if (lockTask.endTime == null) {
                toast("请选择结束时间")
                return@setOnClickListener
            }
            (requireActivity() as MainActivity).lockTaskViewModel.updateLockTasks(lockTask)
            toast("编辑成功")
            findNavController(it).navigateUp()
        }

        binding.etStartTime.setOnClickListener {
            TimePickerFragment(this).show(
                requireActivity().supportFragmentManager,
                TIME_PICKER_TAG_START_TIME
            )
        }

        binding.etEndTime.setOnClickListener {
            TimePickerFragment(this).show(
                requireActivity().supportFragmentManager,
                TIME_PICKER_TAG_END_TIME
            )
        }

        val id = arguments?.getLong("lockID") ?: -1
        CoroutineScope(Dispatchers.Default).launch {
            val job = async {
                return@async (requireActivity() as MainActivity).lockTaskViewModel.getLockTaskByID(id)
            }
            lockTask = job.await()
            binding.etStartTime.setText(
                DateFormat.format(
                    "HH:mm",
                    lockTask.startTime
                )
            )
            binding.etEndTime.setText(
                DateFormat.format(
                    "HH:mm",
                    lockTask.endTime
                )
            )
        }
    }

    companion object {

        const val TIME_PICKER_TAG_START_TIME = "time_picker_tag_start_time"
        const val TIME_PICKER_TAG_END_TIME = "time_picker_tag_end_time"
    }

    class TimePickerFragment(private val editLockTaskFragment: EditLockTaskFragment) :
        DialogFragment(), TimePickerDialog.OnTimeSetListener {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)

            return TimePickerDialog(
                activity,
                this,
                hour,
                minute,
                DateFormat.is24HourFormat(activity)
            )
        }

        override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
            val c = Calendar.getInstance()
            c[Calendar.HOUR_OF_DAY] = hourOfDay
            c[Calendar.MINUTE] = minute
            when (this.tag) {
                TIME_PICKER_TAG_START_TIME -> {
                    editLockTaskFragment.binding.etStartTime.setText(
                        DateFormat.format(
                            "HH:mm",
                            c.time
                        )
                    )
                    editLockTaskFragment.lockTask.startTime = c.time
                }

                TIME_PICKER_TAG_END_TIME -> {
                    editLockTaskFragment.binding.etEndTime.setText(
                        DateFormat.format(
                            "HH:mm",
                            c.time
                        )
                    )
                    editLockTaskFragment.lockTask.endTime = c.time
                }
            }
        }
    }
}