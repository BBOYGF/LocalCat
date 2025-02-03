import com.felinetech.fast_file.pojo.ServiceInfo
import com.felinetech.fast_file.view_model.HomeViewModel.getTaskPo
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
            val service = ServiceInfo("192.168.3.101", 8080)
//            syncUploadFile(service, taskPo!!)
        }
        job.join()
    }

}