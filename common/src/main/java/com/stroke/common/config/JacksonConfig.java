package com.stroke.common.config;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 全局配置 — XSS 防御 + 日期序列化
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer xssEscapeCustomizer() {
        return builder -> {
            builder.postConfigurer(objectMapper -> {
                // HTML 特殊字符转义，防止 XSS
                objectMapper.getFactory().setCharacterEscapes(new HtmlCharacterEscapes());
                // 日期格式统一
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                objectMapper.registerModule(new JavaTimeModule());
            });
        };
    }

    /**
     * HTML 字符转义器
     * <p>
     * 所有 JSON 字符串输出时自动转义 & < > " '，防止 XSS 注入。
     * </p>
     */
    public static class HtmlCharacterEscapes extends CharacterEscapes {

        private static final int[] ESCAPES = new int[128];

        static {
            // 默认所有字符不转义
            for (int i = 0; i < ESCAPES.length; i++) {
                ESCAPES[i] = CharacterEscapes.ESCAPE_NONE;
            }
            // HTML 特殊字符转义
            ESCAPES['&'] = CharacterEscapes.ESCAPE_STANDARD;
            ESCAPES['<'] = CharacterEscapes.ESCAPE_STANDARD;
            ESCAPES['>'] = CharacterEscapes.ESCAPE_STANDARD;
            ESCAPES['"'] = CharacterEscapes.ESCAPE_STANDARD;
            ESCAPES['\''] = CharacterEscapes.ESCAPE_STANDARD;
        }

        private static final SerializedString AMP = new SerializedString("&amp;");
        private static final SerializedString LT  = new SerializedString("&lt;");
        private static final SerializedString GT  = new SerializedString("&gt;");
        private static final SerializedString QUOT = new SerializedString("&quot;");
        private static final SerializedString APOS = new SerializedString("&#x27;");

        @Override
        public int[] getEscapeCodesForAscii() {
            return ESCAPES;
        }

        @Override
        public SerializableString getEscapeSequence(int ch) {
            switch (ch) {
                case '&': return AMP;
                case '<': return LT;
                case '>': return GT;
                case '"': return QUOT;
                case '\'': return APOS;
                default: return null;
            }
        }
    }
}
