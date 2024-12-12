import com.felinetech.localcat.Constants.FILE_CHUNK_SIZE
import com.felinetech.localcat.Constants.THREAD_COUNT
import com.felinetech.localcat.po.FileChunkEntity
import com.felinetech.localcat.po.FileEntity
import com.felinetech.localcat.pojo.Command
import com.felinetech.localcat.pojo.TaskPo
import com.felinetech.localcat.view_model.HomeViewModel
import com.felinetech.localcat.view_model.HomeViewModel.downFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * 服务端设置
 */
class HomeViewModelService {

    /**
     * 服务端测试
     */
    @Test
    fun serviceTest() = runBlocking {
        val ioScope = CoroutineScope(Dispatchers.IO)
        val job = ioScope.launch {
            val fileEntity: FileEntity? = HomeViewModel.fileEntityDao.getFileById("3bbe0cd2-f52d-4a9f-bf5b-3b1dbec2b824")
            val fileChunkEntities: List<FileChunkEntity> =
                HomeViewModel.fileChunkDao.getFileChunksByFileId(fileEntity!!.fileId)
            val taskPo = TaskPo(fileEntity!!, fileChunkEntities, FILE_CHUNK_SIZE)
            val command = Command(THREAD_COUNT, FILE_CHUNK_SIZE, taskPo)

            val syncedData: TaskPo = HomeViewModel.syncData(command.taskPo)
            command.taskPo = syncedData

            val deferred = downFile(8080, command)
            val result = deferred.await()
            println("执行结果$result")
        }
        job.join()
    }
}