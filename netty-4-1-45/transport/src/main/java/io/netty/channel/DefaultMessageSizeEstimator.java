/*
 * Copyright 2013 The Netty Project
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
package io.netty.channel;

import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;

/**
 * Default {@link MessageSizeEstimator} implementation which supports the estimation of the size of
 * {@link ByteBuf}, {@link ByteBufHolder} and {@link FileRegion}.
 */
public final class DefaultMessageSizeEstimator implements MessageSizeEstimator {

    private static final class HandleImpl implements Handle {
        private final int unknownSize;

        private HandleImpl(int unknownSize) {
            this.unknownSize = unknownSize;
        }

        /**
         * 计算msg的大小
         * @param msg       The message for which the size should be calculated
         * @return
         */
        @Override
        public int size(Object msg) {
            if (msg instanceof ByteBuf) {
                return ((ByteBuf) msg).readableBytes();
            }
            if (msg instanceof ByteBufHolder) {
                return ((ByteBufHolder) msg).content().readableBytes();
            }
            // Q: 为啥FileRegion直接返回0了？
            // A: 首先要明确目的，为啥要算msg的大小，我们计算msg的大小的目的是要计算此msg占用JVM内存占据的大小
            // FileRegion类型并不使用堆内存

            // Q: 那ByteBuf也有可能是使用的直接内存，为啥还返回了size？
            // A: ByteBuf仅从存储的角度看，是使用的直接内存存储，但是如果涉及到一些逻辑运算，校验，还是会加载到堆内存中
            if (msg instanceof FileRegion) {
                return 0;
            }
            return unknownSize;
        }
    }

    /**
     * Return the default implementation which returns {@code 8} for unknown messages.
     */
    public static final MessageSizeEstimator DEFAULT = new DefaultMessageSizeEstimator(8);

    private final Handle handle;

    /**
     * Create a new instance
     *
     * @param unknownSize       The size which is returned for unknown messages.
     */
    public DefaultMessageSizeEstimator(int unknownSize) {
        checkPositiveOrZero(unknownSize, "unknownSize");
        handle = new HandleImpl(unknownSize);
    }

    @Override
    public Handle newHandle() {
        return handle;
    }
}
