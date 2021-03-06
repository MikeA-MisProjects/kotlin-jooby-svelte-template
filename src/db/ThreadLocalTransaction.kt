package db

import java.sql.Connection
import java.sql.SQLException
import javax.sql.DataSource

class Transaction(private val db: DataSource) {
  companion object {
    private val threadContext = ThreadLocal<Transaction>()
    fun current(): Transaction? = threadContext.get()
  }

  private var conn: Connection? = null

  init {
    if (current() != null) throw IllegalStateException("tx is already active")
    threadContext.set(this)
  }

  val connection: Connection
    get() = conn ?: db.connection.also { it.autoCommit = false; conn = it }

  fun close(commit: Boolean) {
    try {
      conn?.apply {
        if (commit) commit() else rollback()
        autoCommit = true
        close()
      }
    } finally {
      conn = null
      detachFromThread()
    }
  }

  fun detachFromThread() {
    threadContext.remove()
  }
}

fun <R> DataSource.withConnection(block: Connection.() -> R): R {
  val tx = Transaction.current()
  try {
    return if (tx != null) tx.connection.block()
    else connection.use(block)
  }
  catch (e: SQLException) {
    throw if (e.message?.contains("unique constraint") == true) AlreadyExistsException(e) else e
  }
}
