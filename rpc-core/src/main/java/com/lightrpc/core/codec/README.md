# 1. 五大参数详解

构造方法通常是：
`new LengthFieldBasedFrameDecoder(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip)`

我们一个个拆解：

#### (1) `maxFrameLength` (最大帧长度)

* **含义**：如果收到的数据包超过这个长度，Netty 会直接抛异常。这是为了防止数据过大导致内存溢出（OOM）。
* **你的设置**：`8 * 1024 * 1024` (8MB)。
* **解读**：如果有人发了一个 100MB 的请求过来，直接拒绝。

#### (2) `lengthFieldOffset` (长度字段的偏移量)

* **含义**：从数据包的**第几个字节开始**，才是我们要找的“长度字段”？
* **计算**：
* Magic (4) + Version (1) + Serializer (1) + Type (1) + ReqId (8) = **15**。


* **你的设置**：`15`。
* **解读**：Netty 收到数据后，会跳过前 15 个字节，指针停在第 16 个字节的位置，准备读取长度。

#### (3) `lengthFieldLength` (长度字段本身的长度)

* **含义**：找到位置后，我该读几个字节作为长度值？是 `byte`、`short` 还是 `int`？
* **计算**：你在 `Encoder` 里写的是 `out.writeInt(len)`，`int` 占 **4** 字节。
* **你的设置**：`4`。
* **解读**：Netty 从第 15 个字节开始，读取 4 个字节，比如读到了整数 `100`。现在 Netty 知道 Body 的长度是 100 了。

#### (4) `lengthAdjustment` (长度修正值) —— **这是最难理解的！**

* **含义**：**如果不调整，Netty 默认认为：从“长度字段”结束后，紧接着就是“长度字段的值”那么多字节。**
* 也就是说：`需要读取的字节数 = 长度字段里的值 + adjustment`。


* **场景 A（你的场景）**：
* 你在 `Encoder` 里写入的长度值是 **Body 的长度** (比如 100)。
* Netty 读完长度字段后，剩余未读的刚好就是 Body。
* 你需要读取的字节数 = 100 + `adjustment`。
* 实际就是 100，所以 `adjustment = 0`。


* **场景 B（复杂场景）**：
* 假设你的 `Encoder` 写入的长度是 **整个包的总长度** (头19 + Body100 = 119)。
* Netty 读出来是 119。
* 但此时指针已经指在 Header 后面了，实际上后面只剩 100 字节了。
* Netty 想读 119 个字节，肯定会报错。
* 这时就需要 `adjustment = -19` (减去 Header 的长度)。


* **你的设置**：`0`。因为你的 `Length` 只代表 Body 长度。

#### (5) `initialBytesToStrip` (跳过字节数)

* **含义**：解析完一个包后，要不要把 Header 扔掉，只把 Body 传给后面的 Handler？
* **场景**：
* 如果填 `19`：后面的 `Handler` 拿到的 `ByteBuf` 就只有 Body 数据（JSON 串）。Header 里的 `ReqID`、`Type` 全部丢弃。
* 如果填 `0`：后面的 `Handler` 拿到的是完整的协议包（Header + Body）。


* **你的设置**：`0`。
* **解读**：你需要 Header 里的 `requestID` 做异步调用，需要 `codec` 字段知道用什么反序列化，所以**不能扔**，必须保留。