package zelgius.com.budgetmanager.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.adapter_entry.view.*
import kotlinx.android.synthetic.main.fragment_entry.*
import zelgius.com.budgetmanager.R
import zelgius.com.budgetmanager.dao.BudgetAndEntry
import zelgius.com.budgetmanager.observe
import zelgius.com.budgetmanager.observeOnce
import zelgius.com.budgetmanager.view.SwipeToDeleteCallback
import zelgius.com.budgetmanager.viewModel.EntryViewModel
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter


/**
 * A simple [Fragment] subclass.
 * Use the [EntryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EntryFragment : Fragment() {
    companion object {
        private val DIFF_CALLBACK = object :
                DiffUtil.ItemCallback<BudgetAndEntry>() {
            // Concert details may have changed if reloaded from the database,
            // but ID is fixed.
            override fun areItemsTheSame(old: BudgetAndEntry,
                                         new: BudgetAndEntry) = old.budget?.id == new.budget?.id && old.entry.id == new.entry.id

            override fun areContentsTheSame(old: BudgetAndEntry,
                                            new: BudgetAndEntry) = old.budget == new.budget && old.entry == new.entry
        }

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment EntryFragment.
         */
        @JvmStatic
        fun newInstance() =
                EntryFragment().apply {
                    arguments = Bundle().apply {}
                }
    }

    private val viewModel by lazy {
        ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(EntryViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_entry, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = Adapter()
        recyclerView.adapter = adapter
        viewModel.getBudgetAndEntryDataSource().observe(this) {
            adapter.submitList(it)
        }
        enableSwipeToDeleteAndUndo(recyclerView, adapter)

    }

    private fun enableSwipeToDeleteAndUndo(recyclerView: RecyclerView, adapter: Adapter) {
        val swipeToDeleteCallback: SwipeToDeleteCallback = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
                val position = viewHolder.adapterPosition
                val item = adapter.getData(position)

                if (item != null) {
                    viewModel.delete(item.entry).observeOnce(this@EntryFragment) {
                        Snackbar
                                .make(coordinatorLayout, "Item was removed from the list.", Snackbar.LENGTH_LONG)
                                .setAction("UNDO") {
                                    item.entry.id = null
                                    viewModel.save(item.entry)
                                }
                                //snackbar.setActionTextColor(Color.YELLOW)
                                .show()
                    }
                }
            }
        }
        val itemTouchhelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchhelper.attachToRecyclerView(recyclerView)
    }

    class Adapter : PagedListAdapter<BudgetAndEntry, BudgetViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder =
                BudgetViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_entry, parent, false))


        override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
            val item = getItem(position)

            val context = holder.itemView.context
            if (item != null) {

                if (position > 0 && getItem(position - 1)?.budget == item.budget) {
                    holder.itemView.budgetName.visibility = View.GONE
                    holder.itemView.budgetTotal.visibility = View.GONE
                } else {
                    holder.itemView.budgetName.visibility = View.VISIBLE
                    holder.itemView.budgetTotal.visibility = View.VISIBLE
                    holder.itemView.budgetName.text = item.budget?.name
                            ?: context.getString(R.string.not_associated)

                    holder.itemView.budgetTotal.text = String.format("%s€", DecimalFormat("0.#").format(item.total))
                }

                holder.itemView.entryName.text =
                        if (item.entry.comment.isEmpty()) ""
                        else item.entry.comment
                holder.itemView.entryAmount.text = String.format("%s€", DecimalFormat("0.#").format(item.entry.amount))


                holder.itemView.entryDate.text = DateTimeFormatter.ISO_LOCAL_DATE.format(item.entry.date)
            }
        }

        fun getData(position: Int) = getItem(position)

    }

    class BudgetViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer


}