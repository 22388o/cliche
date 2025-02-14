import fr.acinq.eclair.channel.PersistentChannelData
import immortan.WalletSecret
import immortan.sqlite.{
  DBInterfaceSQLiteGeneral,
  HostedChannelAnnouncementTable,
  HostedChannelUpdateTable,
  HostedExcludedChannelTable,
  NormalChannelAnnouncementTable,
  NormalChannelUpdateTable,
  NormalExcludedChannelTable,
  SQLiteChainWallet,
  SQLiteChannel,
  SQLiteLNUrlPay,
  SQLiteLog,
  SQLiteNetwork,
  SQLitePayment,
  SQLiteTx,
  SQLiteData,
  DBInit
}
import utils.SQLiteUtils
import fr.acinq.bitcoin.ByteVector32

object DB {
  println("# setting up database")

  val dbname: String =
    WalletSecret(Config.seed).keys.ourNodePrivateKey.publicKey.toString.take(6)
  val sqlitedb = SQLiteUtils.getConnection(dbname, Config.datadir)

  // replace this with something that does migrations properly in the future
  DBInit.createTables(sqlitedb)

  val dbinterface = DBInterfaceSQLiteGeneral(sqlitedb)
  var txDataBag: SQLiteTx = null
  var lnUrlPayBag: SQLiteLNUrlPay = null
  var chainWalletBag: SQLiteChainWallet = null
  var extDataBag: SQLiteData = null

  dbinterface txWrap {
    txDataBag = new SQLiteTx(dbinterface)
    lnUrlPayBag = new SQLiteLNUrlPay(dbinterface)
    chainWalletBag = new SQLiteChainWallet(dbinterface)
    extDataBag = new SQLiteData(dbinterface)
  }

  val logBag = new SQLiteLog(dbinterface)

  val normalBag = new SQLiteNetwork(
    dbinterface,
    NormalChannelUpdateTable,
    NormalChannelAnnouncementTable,
    NormalExcludedChannelTable
  )
  val hostedBag = new SQLiteNetwork(
    dbinterface,
    HostedChannelUpdateTable,
    HostedChannelAnnouncementTable,
    HostedExcludedChannelTable
  )
  val payBag = new SQLitePayment(extDataBag.db, preimageDb = dbinterface) {
    override def addSearchablePayment(
        search: String,
        paymentHash: ByteVector32
    ): Unit = {}
  }

  val chanBag =
    new SQLiteChannel(dbinterface, channelTxFeesDb = extDataBag.db) {
      override def put(
          data: PersistentChannelData
      ): PersistentChannelData = {
        super.put(data)
      }
    }
}
