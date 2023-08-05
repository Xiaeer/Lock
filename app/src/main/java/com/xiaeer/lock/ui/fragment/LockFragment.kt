package com.xiaeer.lock.ui.fragment

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.xiaeer.lock.R
import com.xiaeer.lock.data.room.model.LockTask
import com.xiaeer.lock.databinding.FragmentLockBinding
import com.xiaeer.lock.databinding.ItemLockTaskListBinding
import com.xiaeer.lock.ui.activity.MainActivity
import timber.log.Timber

class LockFragment : Fragment() {

    private var _binding: FragmentLockBinding? = null
    private val binding get() = _binding!!

    private var lockTasks: List<LockTask> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rv.linear().setup {
            addType<LockTask>(R.layout.item_lock_task_list)

            onCreate {
                val itemBinding = getBinding<ItemLockTaskListBinding>()
                itemBinding.scEnable.setOnCheckedChangeListener { _, isChecked ->
                    val data = getModel<LockTask>()
                    if (data.enable != isChecked) {
                        data.enable = isChecked
                        (requireActivity() as MainActivity).lockTaskViewModel.updateLockTasks(data)
                    }
                }
                itemBinding.ivEdit.setOnClickListener {
                    val data = getModel<LockTask>()
                    val bundle = Bundle()
                    bundle.putLong("lockID", data.id)
                    findNavController(it).navigate(R.id.action_lock_to_edit_lock_task_fragment, bundle)
                }
                itemBinding.ivDelete.setOnClickListener {
                    val data = getModel<LockTask>()
                    (requireActivity() as MainActivity).lockTaskViewModel.deleteLockTasks(data)
                }
            }

            onBind {
                val itemBinding = getBinding<ItemLockTaskListBinding>()
                val data = getModel<LockTask>()
                itemBinding.tvTime.text = String.format(
                    "%s-%s",
                    DateFormat.format("HH:mm", data.startTime),
                    DateFormat.format("HH:mm", data.endTime)
                )
                itemBinding.scEnable.isChecked = data.enable
            }
        }

        binding.fabAddLockTasker.setOnClickListener {
            findNavController(it).navigate(R.id.action_lock_to_add_lock_task_fragment)
        }

        (requireActivity() as MainActivity).lockTaskViewModel.allLockTaskLive.observe(
            viewLifecycleOwner
        ) { value ->
            Timber.d("数据更新 从 " + lockTasks.size + " 到 " + value.size)
            lockTasks = value
            binding.rv.models = lockTasks
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}