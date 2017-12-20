package app;

import gui.Gui;
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
import transforms.Point3D;
import transforms.Vec3D;
import utils.MeshGenerator;

import javax.swing.SwingUtilities;

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
        MouseMotionListener, KeyListener
{

    private int width, height, ox, oy;

    private OGLBuffers grid, svetloBuf;
    private OGLTextRenderer textRenderer;
    private boolean poly = false;
    private float cas = 0;
    private Gui gui;

    private int gridShaderProgram, gridLocMat, gridLocSvetlo, gridLocOko;
    private int gridLocSvetla, gridLocMaterialy, gridLocMaterial, gridLocCas, gridLocTextura;
    private int svetlo, material = 0, pocetBodu = 50, delkaSvetla = 3, textura = 0;
    private int svetloShaderProgram, locSvetloMat, locSvetloSvetlo, locSvetloRotacniMat, locSvetloDelka;

    private Camera cam = new Camera();
    private Mat4 proj; // created in reshape()
    private Vec3D poziceOka, barva;
    private List<Mat4> svetla = new ArrayList<>();
    private List<Mat4> materialy = new ArrayList<>();

    private OGLTexture2D t1, t2, t1n, t1h, t2n, t2h;
    private OGLTexture2D.Viewer textureViewer;

    @Override
    public void init(GLAutoDrawable glDrawable)
    {
        // check whether shaders are supported
        gui = new Gui(this);
        SwingUtilities.invokeLater(()
                -> 
                {
                    gui.setVisible(true);
        });

        barva = new Vec3D(1, 0, 0);

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
        locSvetloSvetlo = gl.glGetUniformLocation(svetloShaderProgram, "svetlo");
        locSvetloRotacniMat = gl.glGetUniformLocation(svetloShaderProgram, "rotacniMat");
        locSvetloDelka = gl.glGetUniformLocation(svetloShaderProgram, "vyska");

        gridLocMat = gl.glGetUniformLocation(gridShaderProgram, "mat");
        gridLocSvetlo = gl.glGetUniformLocation(gridShaderProgram, "svetlo");
        gridLocOko = gl.glGetUniformLocation(gridShaderProgram, "oko");

        gridLocCas = gl.glGetUniformLocation(gridShaderProgram, "cas");
        gridLocTextura = gl.glGetUniformLocation(gridShaderProgram, "tex");

        gridLocSvetla = gl.glGetUniformLocation(gridShaderProgram, "svetla");
        gridLocMaterialy = gl.glGetUniformLocation(gridShaderProgram, "materialy");
        gridLocMaterial = gl.glGetUniformLocation(gridShaderProgram, "material");

        cam = cam.withPosition(new Vec3D(5, 5, 2.5))
                .withAzimuth(Math.PI * 1.25)
                .withZenith(Math.PI * -0.125);

        gl.glEnable(GL2GL3.GL_DEPTH_TEST);
        /*gl.glEnable(gl.GL_BLEND);
        gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);*/

        textureViewer = new OGLTexture2D.Viewer(gl);
    }

    void createBuffers(GL2GL3 gl)
    {
        grid = MeshGenerator.generateGrid(pocetBodu, pocetBodu, gl, "inPosition");
        svetloBuf = MeshGenerator.generateGrid(25, 25, gl, "inPosition");

        t1 = new OGLTexture2D(gl, "/textures/stones.jpg");
        t1n = new OGLTexture2D(gl, "/textures/stonesn.png");
        t1h = new OGLTexture2D(gl, "/textures/stonesh.png");
        //https://kcclemo.neocities.org/creating-height-and-normal-maps/
        t2 = new OGLTexture2D(gl, "/textures/bricks.png");
        t2n = new OGLTexture2D(gl, "/textures/bricksn.png");
        t2h = new OGLTexture2D(gl, "/textures/bricksh.png");

        poziceOka = cam.getEye();

        svetla.add(new Mat4(
                new Point3D(5, 5, -3), //pozice světla, 4. nevyužita
                new Point3D(1, 0, 0), //barva světla, 4. nevyužita
                new Point3D(1, 0, 0), //útlumy světla (konstantní, lineární, kvadratický), 4. nevyužita
                new Point3D(-5, -5, 3, 10) //směr svícení světla(xyz) a úhel kuželu světla (w)
        ));
        svetla.add(new Mat4(
                new Point3D(0, 0, 5),
                new Point3D(1, 1, 1),
                new Point3D(0, 1, 0),
                new Point3D(0, 0, -1, 180)
        ));
        svetla.add(new Mat4(
                new Point3D(0, 0, -5),
                new Point3D(1, 1, 1),
                new Point3D(1, 0.1, 0.01),
                new Point3D(0, 0, 1, 50)
        ));

        materialy.add(new Mat4( //vlastní 0
                new Point3D(0.2, 0.2, 0.2), //ambientní barva
                new Point3D(0.7, 0.7, 0.7), //difůzní barva
                new Point3D(1.0, 1.0, 1.0), //speculární barva
                new Point3D(70.0, 0, 0) //lesklost a 3x nic
        ));
        materialy.add(new Mat4( //měď 1
                new Point3D(0.01925, 0.0735, 0.0225),
                new Point3D(0.7038, 0.27048, 0.0828),
                new Point3D(0.256777, 0.137622, 0.086014),
                new Point3D(12.8, 0, 0, 0)
        ));
        materialy.add(new Mat4( //černá guma 2
                new Point3D(0.02, 0.02, 0.02),
                new Point3D(0.01, 0.01, 0.01),
                new Point3D(0.4, 0.4, 0.4),
                new Point3D(10, 0, 0, 0)
        ));
        materialy.add(new Mat4( //chrom 3
                new Point3D(0.25, 0.25, 0.25),
                new Point3D(0.4, 0.4, 0.4),
                new Point3D(0.774597, 0.774597, 0.774597),
                new Point3D(76.8, 0, 0, 0)
        ));
        materialy.add(new Mat4( //leštěné zlato 4
                new Point3D(0.24725, 0.1995, 0.0745),
                new Point3D(0.75164, 0.60648, 0.22648),
                new Point3D(0.628281, 0.555802, 0.366065),
                new Point3D(51.2, 0, 0, 0)
        ));
        materialy.add(new Mat4( //emerald 5
                new Point3D(0.0215, 0.1745, 0.0215, 0.55),
                new Point3D(0.07568, 0.61424, 0.07568, 0.55),
                new Point3D(0.633, 0.727811, 0.633, 0.55),
                new Point3D(76.8, 0, 0, 0)
        ));
        materialy.add(new Mat4( //rubín 6
                new Point3D(0.1745, 0.01175, 0.01175, 0.55),
                new Point3D(0.61424, 0.04136, 0.04136, 0.55),
                new Point3D(0.727811, 0.626959, 0.626959, 0.55),
                new Point3D(76.8, 0, 0, 0)
        ));
        materialy.add(new Mat4( //tyrkys 7
                new Point3D(0.1, 0.18725, 0.1745, 0.8),
                new Point3D(0.396, 0.74151, 0.69102, 0.8),
                new Point3D(0.297254, 0.30829, 0.306678, 0.8),
                new Point3D(12.8, 0, 0, 0)
        ));

        svetlo = 0;
    }

    private Mat3 rotacniMat(int cisloSvetla)
    {
        Vec3D smer = new Vec3D(svetla.get(cisloSvetla).getRow(3));
        smer = smer.normalized().orElse(new Vec3D(0, 0, 0));
        Vec3D nahoru = new Vec3D(0, 0, 1);
        Vec3D osaX = nahoru.cross(smer);
        if (osaX.eEquals(new Vec3D(0.0)))
        {
            osaX = new Vec3D(1.0, 0, 0);
        }
        Vec3D osaY = smer.cross(osaX);

        return new Mat3(osaX, osaY, smer);
    }

    public void prenastav(GL2GL3 gl)
    {
        gui.update();
        cas += 0.1;
        double pom = Math.cos((double) cas) * 0.5 + 0.5;

        svetla.set(0, new Mat4(
                new Point3D(5, 5, -3),
                new Point3D(barva),
                new Point3D(2, 0, 0),
                new Point3D(-5, -5, 3, 10 * pom)
        ));

        grid = MeshGenerator.generateGrid(pocetBodu, pocetBodu, gl, "inPosition");
        poziceOka = cam.getEye();
    }

    @Override
    public void display(GLAutoDrawable glDrawable)
    {
        GL2GL3 gl = glDrawable.getGL().getGL2GL3();

        prenastav(gl);

        gl.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        gl.glClear(GL2GL3.GL_COLOR_BUFFER_BIT | GL2GL3.GL_DEPTH_BUFFER_BIT);

        for (int i = 0; i < svetla.size(); i++)
        {
            gl.glUseProgram(svetloShaderProgram);
            gl.glUniformMatrix4fv(locSvetloMat, 1, false,
                    ToFloatArray.convert(cam.getViewMatrix().mul(proj)), 0);
            gl.glUniformMatrix4fv(locSvetloSvetlo, 1, false, ToFloatArray.convert(svetla.get(i)), 0);
            gl.glUniformMatrix3fv(locSvetloRotacniMat, 1, false, ToFloatArray.convert(ToFloatArray.convert(rotacniMat(i))), 0);
            gl.glUniform1i(locSvetloDelka, delkaSvetla);

            svetloBuf.draw(GL2GL3.GL_TRIANGLES, svetloShaderProgram);
        }

        gl.glUseProgram(gridShaderProgram);
        gl.glUniformMatrix4fv(gridLocMat, 1, false,
                ToFloatArray.convert(cam.getViewMatrix().mul(proj)), 0);
        gl.glUniform3fv(gridLocOko, 1, ToFloatArray.convert(poziceOka), 0);
        gl.glUniformMatrix4fv(gridLocSvetla, svetla.size(), false, ToFloatArray.convert(svetla), 0);
        gl.glUniformMatrix4fv(gridLocMaterialy, materialy.size(), false, ToFloatArray.convert(materialy), 0);

        gl.glUniform1f(gridLocSvetlo, svetlo);
        gl.glUniform1i(gridLocMaterial, material);
        gl.glUniform1i(gridLocTextura, textura);
        gl.glUniform1f(gridLocCas, cas);

        t1.bind(gridShaderProgram, "tex1", 0);
        t1n.bind(gridShaderProgram, "tex1Normal", 1);
        t1h.bind(gridShaderProgram, "tex1Vyska", 2);

        t2.bind(gridShaderProgram, "tex2", 3);
        t2n.bind(gridShaderProgram, "tex2Normal", 4);
        t2h.bind(gridShaderProgram, "tex2Vyska", 5);

        if (poly)
        {
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
            int height)
    {
        this.width = width;
        this.height = height;
        proj = new Mat4PerspRH(Math.PI / 4, height / (double) width, 0.01, 1000.0);
        textRenderer.updateSize(width, height);
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        ox = e.getX();
        oy = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        cam = cam.addAzimuth((double) Math.PI * (ox - e.getX()) / width)
                .addZenith((double) Math.PI * (e.getY() - oy) / width);
        ox = e.getX();
        oy = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        switch (e.getKeyCode())
        {
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
            case KeyEvent.VK_NUMPAD9:
                if (pocetBodu < 100)
                {
                    pocetBodu++;
                }
                break;
            case KeyEvent.VK_NUMPAD8:
                if (pocetBodu > 4)
                {
                    pocetBodu--;
                }
                break;

        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
    }

    @Override
    public void dispose(GLAutoDrawable glDrawable)
    {
        glDrawable.getGL().getGL2GL3().glDeleteProgram(gridShaderProgram);
        glDrawable.getGL().getGL2GL3().glDeleteProgram(svetloShaderProgram);
    }

    public int getSvetlo()
    {
        return svetlo;
    }

    public void setSvetlo(int svetlo)
    {
        this.svetlo = svetlo;
    }

    public boolean isPoly()
    {
        return poly;
    }

    public void setPoly(boolean poly)
    {
        this.poly = poly;
    }

    public int getMaterial()
    {
        return material;
    }

    public void setMaterial(int material)
    {
        this.material = material;
    }

    public int getPocetBodu()
    {
        return pocetBodu;
    }

    public void setPocetBodu(int pocetBodu)
    {
        this.pocetBodu = pocetBodu;
    }

    public Vec3D getBarva()
    {
        return barva;
    }

    public void setBarva(Vec3D barva)
    {
        this.barva = barva;
    }

    public int getDelkaSvetla()
    {
        return delkaSvetla;
    }

    public void setDelkaSvetla(int delkaSvetla)
    {
        this.delkaSvetla = delkaSvetla;
    }

    public int getTextura()
    {
        return textura;
    }

    public void setTextura(int textura)
    {
        this.textura = textura;
    }
}
