package zelgius.com.budgetmanager.viewModel

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.toLiveData
import kotlinx.coroutines.launch
import zelgius.com.budgetmanager.entities.Budget
import zelgius.com.budgetmanager.entities.BudgetPart
import zelgius.com.budgetmanager.repositories.BudgetPartRepository
import zelgius.com.budgetmanager.repositories.BudgetRepository

class BudgetViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = BudgetRepository(app)
    private val repositoryPart = BudgetPartRepository(app)

    fun getPagedList() = repositoryPart.getDataSource().toLiveData(pageSize = 50)

    fun save(budget: Budget): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch {

            if (budget.id == null)
                repository.insert(budget)
            else
                repository.update(budget)

            result.postValue(true)
        }

        return result
    }

    fun save(budget: Budget, part: BudgetPart): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch {
            part.refBudget = budget.id

            if (part.id == null)
                repositoryPart.insert(part)
            else
                repositoryPart.update(part)

            result.postValue(true)
        }

        return result
    }

    fun save(part: BudgetPart): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch {

            if (part.id == null)
                repositoryPart.insert(part)
            else
                repositoryPart.update(part)

            result.postValue(true)
        }

        return result
    }

    fun closeBudget(closed: Boolean, budget: Budget): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch {
            repository.update(budget.apply { this.closed = closed })
            repository.closeBudget(closed, budget)
            result.postValue(true)
        }

        return result
    }

    fun delete(budget: Budget): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch {
            repository.delete(budget)
            result.postValue(true)
        }

        return result
    }

    fun delete(budgetPart: BudgetPart): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch {
            repositoryPart.delete(budgetPart)
            result.postValue(true)
        }

        return result
    }

    fun get(closed: Boolean): LiveData<List<Budget>> {
        val result = MutableLiveData<List<Budget>>()
        viewModelScope.launch {
            result.postValue(repository.get(closed))
        }

        return result
    }


    fun get(id: Long): LiveData<Budget?> {
        val result = MutableLiveData<Budget?>()
        viewModelScope.launch {
            result.postValue(repository.get(id))
        }

        return result
    }

    fun getPart(budget: Budget): LiveData<List<BudgetPart>> {
        val result = MutableLiveData<List<BudgetPart>>()
        viewModelScope.launch {
            result.postValue(repositoryPart.get(budget.id!!))
        }

        return result
    }

    fun getPart(budgetId: Long, greaterThanZero: Boolean = false): LiveData<List<BudgetPart>> {
        val result = MutableLiveData<List<BudgetPart>>()
        viewModelScope.launch {
            result.postValue(
                    if (greaterThanZero)
                        repositoryPart.getGreaterThanZero(budgetId)
                    else
                        repositoryPart.get(budgetId)
            )
        }

        return result
    }
}