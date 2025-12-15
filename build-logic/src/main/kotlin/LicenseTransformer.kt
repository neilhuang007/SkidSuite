import com.github.jengelman.gradle.plugins.shadow.transformers.ResourceTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext
import org.gradle.api.file.FileTreeElement
import org.gradle.api.tasks.Input
import java.io.ByteArrayOutputStream
import org.apache.tools.zip.ZipEntry
import org.apache.tools.zip.ZipOutputStream

class LicenseTransformer : ResourceTransformer {

    private val include = linkedSetOf<String>()
    private val exclude = linkedSetOf<String>()
    private val seen = linkedSetOf<String>()
    private val data = ByteArrayOutputStream()

    @get:Input
    lateinit var destinationPath: String

    fun getName(): String = "LicenseTransformer"

    override fun canTransformResource(element: FileTreeElement): Boolean {
        val path = element.relativePath.pathString
        return include.contains(path) && !exclude.contains(path)
    }

    override fun hasTransformedResource(): Boolean = data.size() > 0

    override fun transform(context: TransformerContext) {
        val content = context.inputStream
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
            .replace("\r\n", "\n")

        val trimmed = content.trim()
        if (trimmed.isNotEmpty() && seen.add(trimmed)) {
            data.write(content.toByteArray(Charsets.UTF_8))
            data.write("\n${"-".repeat(20)}\n\n".toByteArray(Charsets.UTF_8))
        }
    }

    override fun modifyOutputStream(os: ZipOutputStream, preserveFileTimestamps: Boolean) {
        val entry = ZipEntry(destinationPath).apply {
            if (!preserveFileTimestamps) time = 0L
        }
        os.putNextEntry(entry)
        data.writeTo(os)
        os.closeEntry()
        data.reset()
    }

    fun include(vararg paths: String) = include.addAll(paths)
    fun exclude(vararg paths: String) = exclude.addAll(paths)
}
