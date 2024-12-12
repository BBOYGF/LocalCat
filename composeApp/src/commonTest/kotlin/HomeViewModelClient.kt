import com.felinetech.localcat.pojo.ServiceInfo
import com.felinetech.localcat.view_model.HomeViewModel.getTaskPo
import com.felinetech.localcat.view_model.HomeViewModel.syncUploadFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * homeViewModel客户端测试
 */
class HomeViewModelClient {

    @Test
    fun client() = runBlocking {
        val ioScope = CoroutineScope(Dispatchers.IO)
        val job = ioScope.launch {
            val taskPo = getTaskPo()
            val service = ServiceInfo("192.168.3.209", 8080)
            syncUploadFile(service, taskPo!!)
        }
        job.join()
    }

}