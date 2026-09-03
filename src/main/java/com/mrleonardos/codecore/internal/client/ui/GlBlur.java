package com.mrleonardos.codecore.internal.client.ui;

import java.nio.FloatBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLContext;

/**
 * Размытие того, что уже нарисовано под панелью.
 *
 * <p>
 * Кадр копируется в текстуру и рисуется обратно через шейдер, который усредняет соседние пиксели. Двух
 * проходов и промежуточных буферов нет намеренно: одна панель чата не стоит возни с FBO, а девять
 * отсчётов по кресту дают вполне приличное стекло.
 *
 * <p>
 * Шейдеры есть не везде, поэтому {@code available()} спрашивают до вызова: без них панель просто рисуется
 * плотнее. Молча оставлять прозрачную дыру нельзя: на светлом фоне текст станет нечитаемым.
 */
final class GlBlur {

    private static final String VERTEX_SHADER = "varying vec2 uv;" + "void main() {"
        + "  gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;"
        + "  uv = gl_MultiTexCoord0.xy;"
        + "}";

    private static final String FRAGMENT_SHADER = "uniform sampler2D screen;" + "uniform vec2 texel;"
        + "uniform float radius;"
        + "varying vec2 uv;"
        + "void main() {"
        + "  vec4 sum = texture2D(screen, uv) * 0.25;"
        + "  sum += texture2D(screen, uv + vec2(texel.x, 0.0) * radius) * 0.125;"
        + "  sum += texture2D(screen, uv - vec2(texel.x, 0.0) * radius) * 0.125;"
        + "  sum += texture2D(screen, uv + vec2(0.0, texel.y) * radius) * 0.125;"
        + "  sum += texture2D(screen, uv - vec2(0.0, texel.y) * radius) * 0.125;"
        + "  sum += texture2D(screen, uv + texel * radius * 0.7) * 0.0625;"
        + "  sum += texture2D(screen, uv - texel * radius * 0.7) * 0.0625;"
        + "  sum += texture2D(screen, uv + vec2(texel.x, -texel.y) * radius * 0.7) * 0.0625;"
        + "  sum += texture2D(screen, uv + vec2(-texel.x, texel.y) * radius * 0.7) * 0.0625;"
        + "  gl_FragColor = vec4(sum.rgb, 1.0);"
        + "}";

    private static final String SCREEN_UNIFORM = "screen";
    private static final String TEXEL_UNIFORM = "texel";
    private static final String RADIUS_UNIFORM = "radius";
    private static final int NOT_READY = 0;
    private static final int FAILED = -1;

    private static int program = NOT_READY;
    private static int texture = NOT_READY;
    private static int textureWidth;
    private static int textureHeight;

    private GlBlur() {}

    /** Умеет ли эта видеокарта то, что нужно для размытия. */
    static boolean available() {
        return GLContext.getCapabilities().OpenGL20 && program != FAILED;
    }

    /**
     * Размыть содержимое экрана под указанным прямоугольником.
     *
     * @param strength радиус размытия в пикселях экрана
     * @return {@code false}, если размыть не вышло; тогда вызывающий рисует обычную подложку
     */
    static boolean draw(int left, int top, int right, int bottom, float strength) {
        if (!available() || right <= left || bottom <= top) {
            return false;
        }
        if (!prepare()) {
            return false;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        ScaledResolution resolution = new ScaledResolution(minecraft, minecraft.displayWidth, minecraft.displayHeight);
        int scale = resolution.getScaleFactor();

        capture(minecraft.displayWidth, minecraft.displayHeight);

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL20.glUseProgram(program);
        GL20.glUniform1i(GL20.glGetUniformLocation(program, SCREEN_UNIFORM), 0);
        GL20.glUniform2f(GL20.glGetUniformLocation(program, TEXEL_UNIFORM), 1F / textureWidth, 1F / textureHeight);
        GL20.glUniform1f(GL20.glGetUniformLocation(program, RADIUS_UNIFORM), strength);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glColor4f(1F, 1F, 1F, 1F);

        float u0 = left * scale / (float) textureWidth;
        float u1 = right * scale / (float) textureWidth;
        float v0 = 1F - top * scale / (float) textureHeight;
        float v1 = 1F - bottom * scale / (float) textureHeight;

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(left, bottom, 0, u0, v1);
        tessellator.addVertexWithUV(right, bottom, 0, u1, v1);
        tessellator.addVertexWithUV(right, top, 0, u1, v0);
        tessellator.addVertexWithUV(left, top, 0, u0, v0);
        tessellator.draw();

        GL20.glUseProgram(0);
        GL11.glPopAttrib();
        return true;
    }

    private static boolean prepare() {
        if (program > NOT_READY) {
            return true;
        }
        if (program == FAILED) {
            return false;
        }

        int vertex = compile(GL20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragment = compile(GL20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        if (vertex == FAILED || fragment == FAILED) {
            program = FAILED;
            return false;
        }

        int created = GL20.glCreateProgram();
        GL20.glAttachShader(created, vertex);
        GL20.glAttachShader(created, fragment);
        GL20.glLinkProgram(created);
        GL20.glDeleteShader(vertex);
        GL20.glDeleteShader(fragment);

        if (GL20.glGetProgrami(created, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            GL20.glDeleteProgram(created);
            program = FAILED;
            return false;
        }

        program = created;
        texture = GL11.glGenTextures();
        return true;
    }

    private static int compile(int type, String source) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            GL20.glDeleteShader(shader);
            return FAILED;
        }
        return shader;
    }

    private static void capture(int width, int height) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        if (width != textureWidth || height != textureHeight) {
            textureWidth = width;
            textureHeight = height;
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
            GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D,
                0,
                GL11.GL_RGB,
                width,
                height,
                0,
                GL11.GL_RGB,
                GL11.GL_UNSIGNED_BYTE,
                (FloatBuffer) null);
        }
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);
    }

}
