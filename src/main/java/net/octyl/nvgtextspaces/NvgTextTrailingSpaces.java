package net.octyl.nvgtextspaces;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwGetWindowContentScale;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWaitEvents;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_CENTER;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_LEFT;
import static org.lwjgl.nanovg.NanoVG.nvgBeginFrame;
import static org.lwjgl.nanovg.NanoVG.nvgCreateFont;
import static org.lwjgl.nanovg.NanoVG.nvgEndFrame;
import static org.lwjgl.nanovg.NanoVG.nvgFillColor;
import static org.lwjgl.nanovg.NanoVG.nvgFontBlur;
import static org.lwjgl.nanovg.NanoVG.nvgFontFace;
import static org.lwjgl.nanovg.NanoVG.nvgFontSize;
import static org.lwjgl.nanovg.NanoVG.nvgText;
import static org.lwjgl.nanovg.NanoVG.nvgTextAlign;
import static org.lwjgl.nanovg.NanoVGGL2.nvgCreate;
import static org.lwjgl.nanovg.NanoVGGL2.nvgDelete;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.memUTF8;

public class NvgTextTrailingSpaces {
    private static long window = NULL;
    private static long nvg = NULL;

    public static void main(String[] args) {
        if (!glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW.");
        }
        try (MemoryStack ignored = MemoryStack.stackPush()) {
            glfwSetErrorCallback(GLFWErrorCallback.createPrint());
            setup();
            run();
        } finally {
            if (window != NULL) {
                glfwDestroyWindow(window);
            }
            if (nvg != NULL) {
                nvgDelete(nvg);
            }
            glfwTerminate();
        }
    }

    private static void setup() {
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);
        window = glfwCreateWindow(640, 480, "Trailing Spaces", NULL, NULL);
        if (window == NULL) {
            throw new IllegalStateException("Failed to create window.");
        }
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glfwSetKeyCallback(window, (window1, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window1, true);
            }
        });
        glfwSwapInterval(1);
        nvg = nvgCreate(0);
        if (nvg == NULL) {
            throw new IllegalStateException("Failed to create NVG context");
        }
        nvgCreateFont(nvg, "sans", "./src/main/resources/Open_Sans/OpenSans-Regular.ttf");
    }

    private static final NVGColor RED = NVGColor.calloc()
        .a(1).r(1).g(0).b(0);

    private static void run() {
        MemoryStack stack = MemoryStack.stackGet();
        IntBuffer fbWidth = stack.mallocInt(1);
        IntBuffer fbHeight = stack.mallocInt(1);
        FloatBuffer contentWidth = stack.mallocFloat(1);
        FloatBuffer contentHeight = stack.mallocFloat(1);
        while (!glfwWindowShouldClose(window)) {
            glfwWaitEvents();

            glfwGetFramebufferSize(window, fbWidth, fbHeight);
            glfwGetWindowContentScale(window, contentWidth, contentHeight);

            int effecWidth = (int) (fbWidth.get(0) / contentWidth.get(0));
            int effecHeight = (int) (fbHeight.get(0) / contentHeight.get(0));

            glViewport(0, 0, fbWidth.get(0), fbHeight.get(0));
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

            nvgBeginFrame(nvg, effecWidth, effecHeight, Math.max(contentWidth.get(0), contentHeight.get(0)));

            ByteBuffer text = memUTF8("Spaces:               ", false);
            drawText(50, 50, text, 18, 0, "sans", RED, NVG_ALIGN_CENTER);
            memFree(text);
            // show what this would look like if spaces were trimmed
            text = memUTF8("Nospcs:", false);
            drawText(50, 75, text, 18, 0, "sans", RED, NVG_ALIGN_CENTER);
            memFree(text);

            nvgEndFrame(nvg);

            glfwSwapBuffers(window);
        }
    }

    private static void drawText(int posX, int posY, ByteBuffer asciiBuffer,
                                 float fontSize, float fontBlur, String font, NVGColor color, int alignment) {
        nvgFontSize(nvg, fontSize);
        nvgFontFace(nvg, font);
        nvgFontBlur(nvg, fontBlur);
        nvgFillColor(nvg, color);
        nvgTextAlign(nvg, alignment);
        nvgText(nvg, (float)posX, (float)posY, asciiBuffer);
    }

}
