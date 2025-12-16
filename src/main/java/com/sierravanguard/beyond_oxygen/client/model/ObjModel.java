package com.sierravanguard.beyond_oxygen.client.model;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ObjModel {
    public static ObjModel load(BufferedReader reader) {
        Loader loader = new Loader();
        int row = 0;
        try {
            String line;
            while ((line = reader.readLine()) != null) loader.parseLine(line);
        } catch (Throwable t) {
            throw new IllegalStateException("Failed to load an obj file. Errored at line " + row, t);
        }
        return new ObjModel(loader.quads.toArray(Quad[]::new));
    }

    public static ObjModel load(ResourceManager resourceManager, ResourceLocation path) throws IOException {
        Resource resource = resourceManager.getResourceOrThrow(path);
        return load(resource.openAsReader());
    }

    public final Quad[] quads;

    public ObjModel(Quad[] quads) {
        this.quads = quads;
    }

    public void visit(VertexVisitor operation, FloatUnaryOperator u, FloatUnaryOperator v) {
        for (Quad quad : quads) quad.visit(operation, u, v);
    }

    public void visit(VertexVisitor operation, float u0, float v0, float deltaU, float deltaV) {
        visit(operation, u -> u0 + deltaU * u, v -> v0 + deltaV * v);
    }

    public void visit(VertexVisitor operation, TextureAtlasSprite sprite) {
        visit(operation, sprite.getU0(), sprite.getV0(), sprite.getU1() - sprite.getU0(), sprite.getV1() - sprite.getV0());
    }

    public void visit(VertexVisitor operation) {
        visit(operation, FloatUnaryOperator.identity(), FloatUnaryOperator.identity());
    }

    public record Vertex(Vector3f position, Vector3f normal, Vector2f tex) {
        public void visit(VertexVisitor operation, FloatUnaryOperator u, FloatUnaryOperator v) {
            operation.visit(position.x, position.y, position.z, normal.x, normal.y, normal.z, u.apply(tex.x), v.apply(tex.y));
        }

        public void visit(VertexVisitor operation, float u0, float v0, float deltaU, float deltaV) {
            visit(operation, u -> u0 + deltaU * u, v -> v0 + deltaV * v);
        }

        public void visit(VertexVisitor operation, TextureAtlasSprite sprite) {
            visit(operation, sprite.getU0(), sprite.getV0(), sprite.getU1() - sprite.getU0(), sprite.getV1() - sprite.getV0());
        }

        public void visit(VertexVisitor operation) {
            visit(operation, FloatUnaryOperator.identity(), FloatUnaryOperator.identity());
        }
    }

    public record Quad(Vertex a, Vertex b, Vertex c, Vertex d) {
        public void visit(VertexVisitor operation, FloatUnaryOperator u, FloatUnaryOperator v) {
            a.visit(operation, u, v);
            b.visit(operation, u, v);
            c.visit(operation, u, v);
            d.visit(operation, u, v);
        }

        public void visit(VertexVisitor operation, float u0, float v0, float deltaU, float deltaV) {
            visit(operation, u -> u0 + deltaU * u, v -> v0 + deltaV * v);
        }

        public void visit(VertexVisitor operation, TextureAtlasSprite sprite) {
            visit(operation, sprite.getU0(), sprite.getV0(), sprite.getU1() - sprite.getU0(), sprite.getV1() - sprite.getV0());
        }

        public void visit(VertexVisitor operation) {
            visit(operation, FloatUnaryOperator.identity(), FloatUnaryOperator.identity());
        }
    }

    public static class Loader {
        // position/tex/normal
        private final List<Vector3f> positions = new ArrayList<>();
        private final List<Vector3f> normals = new ArrayList<>();
        private final List<Vector2f> uvs = new ArrayList<>();
        private final List<Quad> quads = new ArrayList<>();

        public void parseLine(String line) {
            if (line.startsWith("v ")) parseVertex(line.substring(2));
            else if (line.startsWith("vn ")) parseNormal(line.substring(3));
            else if (line.startsWith("vt ")) parseUV(line.substring(3));
            else if (line.startsWith("f ")) parseFace(line.substring(2));
        }

        public void parseVertex(String string) {
            String[] items = string.split(" ");
            if (items.length != 3) throw new IllegalArgumentException("Invalid obj file: vertex definition did not have 3 elements. Has " + items.length);
            positions.add(new Vector3f(
                    Float.parseFloat(items[0]),
                    Float.parseFloat(items[1]),
                    Float.parseFloat(items[2])
            ));
        }

        public void parseNormal(String string) {
            String[] items = string.split(" ");
            if (items.length != 3) throw new IllegalArgumentException("Invalid obj file: normal definition did not have 3 elements. Has " + items.length);
            normals.add(new Vector3f(
                    Float.parseFloat(items[0]),
                    Float.parseFloat(items[1]),
                    Float.parseFloat(items[2])
            ));
        }

        public void parseUV(String string) {
            String[] items = string.split(" ");
            if (items.length != 2) throw new IllegalArgumentException("Invalid obj file: uv definition did not have 2 elements. Has " + items.length);
            uvs.add(new Vector2f(
                    Float.parseFloat(items[0]),
                    1 - Float.parseFloat(items[1])
            ));
        }

        public void parseFace(String string) {
            String[] items = string.split(" ");
            if (items.length != 4) throw new IllegalArgumentException("Invalid obj file: face definition did not have 4 elements. Has " + items.length);
            quads.add(new Quad(
                    parseVertexIndices(items[0]),
                    parseVertexIndices(items[1]),
                    parseVertexIndices(items[2]),
                    parseVertexIndices(items[3])
            ));
        }

        public Vertex parseVertexIndices(String string) {
            String[] items = string.split("/");
            if (items.length != 3) throw new IllegalArgumentException("Invalid obj file: face vertex data did not have 3 elements. Has " + items.length);
            Vector3f position = parseVertexIndex(items[0], "position", positions);
            Vector3f normal = parseVertexIndex(items[2], "normal", normals);
            Vector2f tex = parseVertexIndex(items[1], "tex", uvs);
            return new Vertex(position, normal, tex);
        }

        public <T> T parseVertexIndex(String string, String indexType, List<T> values) {
            int index = Integer.parseInt(string);
            if (index < 0) throw new IllegalArgumentException("Invalid obj file: face vertex " + indexType + " index must be positive, was " + index);
            if (index > values.size()) throw new IllegalArgumentException("Invalid obj file: face vertex " + indexType + " index out of bounds. limit is " + values.size() + ", was " + index);
            return values.get(index - 1);
        }
    }

    @FunctionalInterface
    public interface VertexVisitor {
        void visit(float x, float y, float z, float nX, float nY, float nZ, float u, float v);
    }
}
