/*
 * Copyright 2015 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.protobuf;

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * An encoder that prepends the Google Protocol Buffers
 * <a href="https://developers.google.com/protocol-buffers/docs/encoding?csw=1#varints">Base
 * 128 Varints</a> integer length field. For example:
 * <pre>
 * 给实际数据添加Length头部
 * ----
 * BEFORE ENCODE (300 bytes)       AFTER ENCODE (302 bytes)
 * +---------------+               +--------+---------------+
 * | Protobuf Data |-------------->| Length | Protobuf Data |
 * |  (300 bytes)  |               | 0xAC02 |  (300 bytes)  |
 * +---------------+               +--------+---------------+
 * </pre> *
 *
 * @see CodedOutputStream
 * @see CodedOutputByteBufferNano
 */
@Sharable
public class ProtobufVarint32LengthFieldPrepender extends MessageToByteEncoder<ByteBuf> {

    @Override
    protected void encode(
            ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        // 此处msg的protobuf-java对象序列化后生成的 byte[]，bodyLen就是实际的数据长度
        int bodyLen = msg.readableBytes();
        // 计算使用Varint32 编码 bodyLen这一整数，所需的byte数
        int headerLen = computeRawVarint32Size(bodyLen);
        // 确保out-ByteBuf能写的下 headerLen + bodyLen
        out.ensureWritable(headerLen + bodyLen);

        // 实际给out-ByteBuf添加Length头部信息
        writeRawVarint32(out, bodyLen);

        // 将[ 0xAC02 |  (300 bytes) ] 实际发送出去
        out.writeBytes(msg, msg.readerIndex(), bodyLen);
    }

    /**
     * Writes protobuf varint32 to (@link ByteBuf).
     * @param out to be written to
     * @param value to be written
     */
    static void writeRawVarint32(ByteBuf out, int value) {
        while (true) {
            // 判断当前值是否可以用一个字节表示（即值小于等于 127）。如果是，则直接将该值写入 ByteBuf 并返回
            if ((value & ~0x7F) == 0) {
                out.writeByte(value);
                return;
            } else {
                // 如果不是最后一个字节，则将当前值的低 7 位与 0x80 进行按位或运算，得到一个字节，并将该字节写入 ByteBuf
                out.writeByte((value & 0x7F) | 0x80);
                // 将值右移 7 位，继续处理剩余的部分
                value >>>= 7;
            }
        }
    }

    /**
     * 计算一个int类型整数使用 Varint32 编码所需的字节数
     * Computes size of protobuf varint32 after encoding.
     * @param value which is to be encoded.
     * @return size of value encoded as protobuf varint32.
     */
    static int computeRawVarint32Size(final int value) {
        // 判断值是否小于等于 2^7 - 1，如果是，则返回 1
        if ((value & (0xffffffff <<  7)) == 0) {
            return 1;
        }
        // 判断值是否小于等于 2^14 - 1，如果是，则返回 2
        if ((value & (0xffffffff << 14)) == 0) {
            return 2;
        }
        // .....
        if ((value & (0xffffffff << 21)) == 0) {
            return 3;
        }
        if ((value & (0xffffffff << 28)) == 0) {
            return 4;
        }
        return 5;
    }
}
