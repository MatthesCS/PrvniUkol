package utils;

import com.jogamp.opengl.GL2GL3;
import java.util.ArrayList;
import java.util.List;
/*import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;*/
import oglutils.OGLBuffers;
import oglutils.ToFloatArray;
import oglutils.ToIntArray;
//import org.jetbrains.annotations.NotNull;
import transforms.Vec2D;

public class MeshGenerator
{
    public static OGLBuffers generateGrid(final int m, final int n,  GL2GL3 gl,  final String name)
    {
        
        final List<Vec2D> vertices = new ArrayList<>();
        for (int r = 0; r < m; r++)
        {
            for (int c = 0; c < n; c++)
            {
                vertices.add(new Vec2D(c / (n - 1.0), r / (m - 1.0)));
            }
        }
        /*

        Seq<Vec2D> vertices = Stream.range(0, m).flatMap(
                (final Integer r) -> Stream.range(0, n).map(
                        (final Integer c) ->
                                new Vec2D(c / (n - 1.0), r / (m - 1.0))
                )
        );
*/
        
        List<Integer> indicies = new ArrayList<>();

        for (int r = 0; r < m - 1; r++)
        {
            for (int c = 0; c < n - 1; c++)
            {
                indicies.add(r * n + c);
                indicies.add(r * n + c + 1);
                indicies.add((r + 1) * n + c);
                indicies.add((r + 1) * n + c);
                indicies.add(r * n + c + 1);
                indicies.add((r + 1) * n + c + 1);
            }
        }
        /*

        Seq<Tuple2<Integer, Integer>> offsets = Array.of(0, 0, 1, 1, 0, 1).zip(Array.of(0, 1, 0, 0, 1, 1));
        Seq<Integer> indicies = Stream.range(0, m - 1).flatMap(
                (final Integer r) -> Stream.range(0, n - 1).flatMap(
                        (final Integer c) -> offsets.map(
                                (final Tuple2<Integer, Integer> offset) ->
                                        (r + offset._1) * n + c + offset._2
                        )
                )
        );*/

        final OGLBuffers.Attrib[] attributes =
                {
                        new OGLBuffers.Attrib(name, 2),
                };

        return new OGLBuffers(gl, ToFloatArray.convert(vertices), attributes, ToIntArray.convert(indicies));
    }
}
