package app;

import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;

import oglutils.*;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import transforms.Camera;
import transforms.Mat3;
import transforms.Mat4;
import transforms.Mat4PerspRH;
import transforms.Vec3D;
import utils.MeshGenerator;

/**
 * GLSL sample:<br/>
 * Draw 3D geometry, use camera and projection transformations<br/>
 * Requires JOGL 2.3.0 or newer
 *
 * @author PGRF FIM UHK
 * @version 2.0
 * @since 2015-09-05
 */
public class Renderer implements GLEventListener, MouseListener,
        MouseMotionListener, KeyListener {

    int width, height, ox, oy;

    OGLBuffers grid, svetloBuf;
    OGLTextRenderer textRenderer;
    boolean poly = false;

    int gridShaderProgram, gridLocMat, gridLocSvetlo, gridLocOko;
    int gridLocDifBarva, gridLocSpecBarva, gridLocAmbBarva, gridLocLesklost;
    int gridLocSvetla;
    int svetlo, pocetBodu = 50;
    int svetloShaderProgram, locSvetloMat, locSvetloPozice, locSvetloBarva;
    float lesklost;

    Camera cam = new Camera();
    Mat4 proj; // created in reshape()
    Vec3D poziceOka, difuzniBarvaSvetla, specularniBarvaSvetla, ambientniBarvaSvetla;
    List<Mat3> svetla = new ArrayList<>();

    OGLTexture2D texture, textureNormal;
    OGLTexture2D.Viewer textureViewer;

    @Override
    public void init(GLAutoDrawable glDrawable) {
        // check whether shaders are supported
        GL2GL3 gl = glDrawable.getGL().getGL2GL3();
        OGLUtils.shaderCheck(gl);

        // get and set debug version of GL class
        gl = OGLUtils.getDebugGL(gl);
        glDrawable.setGL(gl);

        OGLUtils.printOGLparameters(gl);

        textRenderer = new OGLTextRenderer(gl, glDrawable.getSurfaceWidth(), glDrawable.getSurfaceHeight());

        gridShaderProgram = ShaderUtils.loadProgram(gl, "/shader/grid");
        svetloShaderProgram = ShaderUtils.loadProgram(gl, "/shader/svetlo");
        createBuffers(gl);

        locSvetloMat = gl.glGetUniformLocation(svetloShaderProgram, "mat");
        locSvetloPozice = gl.glGetUniformLocation(svetloShaderProgram, "pozice");
        locSvetloBarva = gl.glGetUniformLocation(svetloShaderProgram, "barva");

        gridLocMat = gl.glGetUniformLocation(gridShaderProgram, "mat");
        gridLocSvetlo = gl.glGetUniformLocation(gridShaderProgram, "svetlo");
        gridLocOko = gl.glGetUniformLocation(gridShaderProgram, "oko");
        gridLocLesklost = gl.glGetUniformLocation(gridShaderProgram, "lesklost");

        gridLocDifBarva = gl.glGetUniformLocation(gridShaderProgram, "difBarva");
        gridLocSpecBarva = gl.glGetUniformLocation(gridShaderProgram, "specBarva");
        gridLocAmbBarva = gl.glGetUniformLocation(gridShaderProgram, "ambBarva");
        
        gridLocSvetla = gl.glGetUniformLocation(gridShaderProgram, "svetla");  

        cam = cam.withPosition(new Vec3D(5, 5, 2.5))
                .withAzimuth(Math.PI * 1.25)
                .withZenith(Math.PI * -0.125);

        gl.glEnable(GL2GL3.GL_DEPTH_TEST);

        texture = new OGLTexture2D(gl, "/textures/bricks.jpg");
        textureNormal = new OGLTexture2D(gl, "/textures/bricksn.png");
        textureViewer = new OGLTexture2D.Viewer(gl);
    }

    void createBuffers(GL2GL3 gl) {
        grid = MeshGenerator.generateGrid(pocetBodu, pocetBodu, gl, "inPosition");
        svetloBuf = MeshGenerator.generateGrid(25, 25, gl, "inPosition");

        poziceOka = cam.getEye();
        
        svetla.add(new Mat3(
                new Vec3D(5, 5, -3), //pozice světla
                new Vec3D(0, 0, 1),  //barva světla
                new Vec3D(0, 0, 0) //útlumy světla (konstantní, lineární, kvadratický)
        ));
        svetla.add(new Mat3(
                new Vec3D(0, 0, 5),
                new Vec3D(1, 0, 0),
                new Vec3D(0, 0, 0)
        ));
        svetla.add(new Mat3(
                new Vec3D(0, 0, -5),
                new Vec3D(0, 1, 0),
                new Vec3D(0, 0, 0)
        ));
        

        svetlo = 0;
        
        difuzniBarvaSvetla = new Vec3D(0.7, 0.7, 0.7);//co sežere matroš
        specularniBarvaSvetla = new Vec3D(1.0, 1.0, 1.0);//odražečná
        ambientniBarvaSvetla = new Vec3D(0.2, 0.2, 0.2);//odraz?
        lesklost = 70.0f;
        
        //měď
        /*ambientniBarvaSvetla = new Vec3D(0.01925, 0.0735, 0.0225);
        difuzniBarvaSvetla = new Vec3D(0.7038, 0.27048, 0.0828);
        specularniBarvaSvetla = new Vec3D(0.256777, 0.137622, 0.086014);
        lesklost = 12.8f;*/
        
        //černá guma 
        /*ambientniBarvaSvetla = new Vec3D(0.02, 0.02, 0.02);
        difuzniBarvaSvetla = new Vec3D(0.01, 0.01, 0.01);
        specularniBarvaSvetla = new Vec3D(0.4, 0.4, 0.4);
        lesklost = 10f;
        primeBarvySvetla.add(new Vec3D(1, 1, 1));
        primeBarvySvetla.add(new Vec3D(1, 1, 1));
        primeBarvySvetla.add(new Vec3D(1, 1, 1));*/
    }

    @Override
    public void display(GLAutoDrawable glDrawable) {

        grid = MeshGenerator.generateGrid(pocetBodu, pocetBodu, glDrawable.getGL().getGL2GL3(), "inPosition");
        poziceOka = cam.getEye();
        GL2GL3 gl = glDrawable.getGL().getGL2GL3();

        gl.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        gl.glClear(GL2GL3.GL_COLOR_BUFFER_BIT | GL2GL3.GL_DEPTH_BUFFER_BIT);

        for (int i = 0; i < svetla.size(); i++) {
            gl.glUseProgram(svetloShaderProgram);
            gl.glUniformMatrix4fv(locSvetloMat, 1, false,
                    ToFloatArray.convert(cam.getViewMatrix().mul(proj)), 0);
            gl.glUniform3fv(locSvetloBarva, 1, ToFloatArray.convert(svetla.get(i).getRow(1)), 0);
            gl.glUniform3fv(locSvetloPozice, 1, ToFloatArray.convert(svetla.get(i).getRow(0)), 0);

            svetloBuf.draw(GL2GL3.GL_TRIANGLES, svetloShaderProgram);
        }

        gl.glUseProgram(gridShaderProgram);
        gl.glUniformMatrix4fv(gridLocMat, 1, false,
                ToFloatArray.convert(cam.getViewMatrix().mul(proj)), 0);
        gl.glUniform3fv(gridLocOko, 1, ToFloatArray.convert(poziceOka), 0);
        gl.glUniform3fv(gridLocAmbBarva, 1, ToFloatArray.convert(ambientniBarvaSvetla), 0);
        gl.glUniform3fv(gridLocDifBarva, 1, ToFloatArray.convert(difuzniBarvaSvetla), 0);
        gl.glUniform3fv(gridLocSpecBarva, 1, ToFloatArray.convert(specularniBarvaSvetla), 0);
        gl.glUniform1f(gridLocLesklost, lesklost);
        gl.glUniformMatrix3fv(gridLocSvetla, svetla.size(), false, ToFloatArray.convert(svetla), 0);

        gl.glUniform1f(gridLocSvetlo, svetlo);
        
        gl.glTexParameteri(GL2GL3.GL_TEXTURE_2D, GL2GL3.GL_TEXTURE_WRAP_S, GL2GL3.GL_REPEAT);
        gl.glTexParameteri(GL2GL3.GL_TEXTURE_2D, GL2GL3.GL_TEXTURE_WRAP_T, GL2GL3.GL_REPEAT);

        texture.bind(gridShaderProgram, "textura", 0);
        textureNormal.bind(gridShaderProgram, "texturaNormal", 1);

        if (poly) {
            gl.glPolygonMode(GL2GL3.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
        }

        grid.draw(GL2GL3.GL_TRIANGLES, gridShaderProgram);

        //textureViewer.view(texture, -1, -1, 0.5);
        //textureViewer.view(textureNormal, -1, -1, 0.5);
        
        String text = new String(this.getClass().getName() + ": [LMB] camera, WSAD");

        textRenderer.drawStr2D(3, height - 20, text);
        textRenderer.drawStr2D(width - 90, 3, " (c) PGRF UHK");
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width,
            int height) {
        this.width = width;
        this.height = height;
        proj = new Mat4PerspRH(Math.PI / 4, height / (double) width, 0.01, 1000.0);
        textRenderer.updateSize(width, height);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        ox = e.getX();
        oy = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        cam = cam.addAzimuth((double) Math.PI * (ox - e.getX()) / width)
                .addZenith((double) Math.PI * (e.getY() - oy) / width);
        ox = e.getX();
        oy = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                cam = cam.forward(1);
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                cam = cam.right(1);
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                cam = cam.backward(1);
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                cam = cam.left(1);
                break;
            case KeyEvent.VK_CONTROL:
                cam = cam.down(1);
                break;
            case KeyEvent.VK_SHIFT:
                cam = cam.up(1);
                break;
            case KeyEvent.VK_SPACE:
                cam = cam.withFirstPerson(!cam.getFirstPerson());
                break;
            case KeyEvent.VK_R:
                cam = cam.mulRadius(0.9f);
                break;
            case KeyEvent.VK_F:
                cam = cam.mulRadius(1.1f);
                break;
            case KeyEvent.VK_P:
                poly = !poly;
                break;
            case KeyEvent.VK_L:
                svetlo++;
                if (svetlo > 4) {
                    svetlo = 0;
                }
                break;
            case KeyEvent.VK_NUMPAD9:
                if (pocetBodu < 100) {
                    pocetBodu++;
                }
                break;
            case KeyEvent.VK_NUMPAD8:
                if (pocetBodu > 4) {
                    pocetBodu--;
                }
                break;

        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void dispose(GLAutoDrawable glDrawable) {
        glDrawable.getGL().getGL2GL3().glDeleteProgram(gridShaderProgram);
        glDrawable.getGL().getGL2GL3().glDeleteProgram(svetloShaderProgram);
    }

}
