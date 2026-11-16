package com.comp2042;

import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.transform.Scale;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/** Plays over a snapshot of the current game-over screen. */
final class ReviveAnimation {
    private static final System.Logger LOG = System.getLogger(ReviveAnimation.class.getName());
    private static final double WIDTH = 1280;
    private static final double HEIGHT = 720;
    private static final double CRACK = 1.24;
    private static final double SHATTER = 2.24;
    private static final double IMPACT = 3.18;
    private static final double END = 3.58;
    private static final Color PURPLE = Color.web("#dd8aff");
    private static final Color GOLD = Color.web("#ffe18a");
    private static final Color SHATTER_BACKGROUND = Color.web("#0d0815");
    private static final Color CRACK_COLOR = Color.web("#f1dcff");
    private static final Color CRACK_GLOW = Color.color(.65, .25, .94, .35);
    private final StackPane root;
    private final Canvas overlay = new Canvas();
    private final GraphicsContext graphics = overlay.getGraphicsContext2D();
    private final List<Point[]> rays = new ArrayList<>();
    private final List<Shard> shards = new ArrayList<>();
    private Transition transition;
    private Runnable completion;
    private Window watchedWindow;
    private boolean interrupted;
    private final ChangeListener<Boolean> focusListener = (property, previous, focused) -> focusChanged(focused);
    private Image screen;
    private Image card;
    private Image brightCard;
    private Image[] shardImages;
    private final Image aura = makeAura();
    private Point origin;
    private double originWidth;
    private double originHeight;
    private double finalWidth;
    private static final Point CENTER = new Point(WIDTH / 2, HEIGHT / 2);

    ReviveAnimation(StackPane root) {
        this.root = root;
        overlay.setVisible(false);
        overlay.setManaged(false);
        overlay.setWidth(WIDTH);
        overlay.setHeight(HEIGHT);
        Scale fitToWindow = new Scale();
        fitToWindow.xProperty().bind(root.widthProperty().divide(WIDTH));
        fitToWindow.yProperty().bind(root.heightProperty().divide(HEIGHT));
        overlay.getTransforms().add(fitToWindow);
        root.getChildren().add(overlay);
        prepareCracks();
    }

    void play(Node source, Image card, Runnable onFinished) {
        stop();
        if (root.getScene() == null || root.getWidth() <= 0 || root.getHeight() <= 0) {
            onFinished.run();
            return;
        }
        completion = onFinished;
        Window window = root.getScene().getWindow();
        if (window != null && window.isShowing()) {
            watchedWindow = window;
            watchedWindow.focusedProperty().addListener(focusListener);
            if (!watchedWindow.isFocused()) {
                interrupted = true;
                return;
            }
        }
        try {
            capture(source, card, onFinished);
        } catch (RuntimeException error) {
            recover(onFinished, error);
        }
    }

    private void capture(Node source, Image card, Runnable onFinished) {
        if (this.card == null) {
            this.card = new Image(card.getUrl(), 420, 525, true, true);
            brightCard = brighten(this.card);
        }
        root.applyCss();
        root.layout();
        double sx = WIDTH / root.getWidth();
        double sy = HEIGHT / root.getHeight();
        if (source != null) {
            Bounds bounds = root.sceneToLocal(source.localToScene(source.getBoundsInLocal()));
            origin = new Point(bounds.getCenterX() * sx, bounds.getCenterY() * sy);
            originWidth = bounds.getWidth() * sx;
            originHeight = bounds.getHeight() * sy;
        } else {
            origin = new Point(105, 530);
            originWidth = 110;
            originHeight = originWidth * 1.25 * sy / sx;
        }
        finalWidth = (HEIGHT * 2 / 3) / 1.25 * sx / sy;
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setTransform(new Scale(sx, sy));
        root.snapshot(result -> {
            if (completion == onFinished && !interrupted) {
                try {
                    startPlayback(result.getImage(), onFinished);
                } catch (RuntimeException error) {
                    recover(onFinished, error);
                }
            }
            return null;
        }, parameters, new WritableImage((int) WIDTH, (int) HEIGHT));
    }

    private void startPlayback(Image snapshot, Runnable onFinished) {
        screen = snapshot;
        shardImages = bakeShards(snapshot);
        overlay.setVisible(true);
        draw(0);
        transition = new Transition() {
            { setCycleDuration(Duration.seconds(END)); }

            @Override
            protected void interpolate(double fraction) {
                try {
                    draw(fraction * END);
                } catch (RuntimeException error) {
                    Platform.runLater(() -> recover(onFinished, error));
                }
            }
        };
        transition.setInterpolator(javafx.animation.Interpolator.LINEAR);
        transition.setOnFinished(event -> finish(onFinished));
        transition.play();
    }

    private void focusChanged(boolean focused) {
        if (completion == null) return;
        if (!focused) {
            interrupted = true;
            if (transition != null) transition.pause();
            overlay.setVisible(false);
        } else if (interrupted) {
            Runnable pending = completion;
            Platform.runLater(() -> {
                if (watchedWindow == null || watchedWindow.isFocused()) {
                    finish(pending);
                }
            });
        }
    }

    private void recover(Runnable pending, RuntimeException error) {
        if (completion != pending) return;
        LOG.log(System.Logger.Level.WARNING, "Revive animation could not finish rendering.", error);
        if (!interrupted) finish(pending);
    }

    private void finish(Runnable pending) {
        if (completion != pending) return;
        stop();
        pending.run();
    }

    void stop() {
        if (watchedWindow != null) {
            watchedWindow.focusedProperty().removeListener(focusListener);
            watchedWindow = null;
        }
        completion = null;
        interrupted = false;
        if (transition != null) {
            transition.stop();
            transition = null;
        }
        screen = null;
        shardImages = null;
        overlay.setVisible(false);
    }

    private void prepareCracks() {
        Point hub = new Point(637, 327);
        Point[] edges = {new Point(0, 0), new Point(300, 0), new Point(665, 0), new Point(982, 0),
                new Point(WIDTH, 0), new Point(WIDTH, 225), new Point(WIDTH, 466), new Point(WIDTH, HEIGHT),
                new Point(1068, HEIGHT), new Point(733, HEIGHT), new Point(430, HEIGHT),
                new Point(0, HEIGHT), new Point(0, 440), new Point(0, 180)};
        for (int i = 0; i < edges.length; i++) {
            Point end = edges[i];
            int bend = i % 2 == 0 ? -1 : 1;
            double inner = .20 + (i * 7 % 9) * .016;
            double outer = .57 + (i * 5 % 9) * .024;
            rays.add(new Point[]{hub,
                    new Point(mix(hub.x, end.x, inner) + bend * 18, mix(hub.y, end.y, inner) - bend * 9),
                    new Point(mix(hub.x, end.x, outer) - bend * 16, mix(hub.y, end.y, outer) + bend * 11),
                    end});
        }
        for (int i = 0; i < rays.size(); i++) {
            Point[] ray = rays.get(i);
            Point[] next = rays.get((i + 1) % rays.size());
            addShard(hub, ray[1], next[1]);
            addShard(ray[1], ray[2], next[2], next[1]);
            addShard(ray[2], ray[3], next[3], next[2]);
        }
    }

    private void addShard(Point... polygon) {
        double minX = WIDTH, minY = HEIGHT, maxX = 0, maxY = 0, x = 0, y = 0;
        for (Point point : polygon) {
            minX = Math.min(minX, point.x);
            minY = Math.min(minY, point.y);
            maxX = Math.max(maxX, point.x);
            maxY = Math.max(maxY, point.y);
            x += point.x;
            y += point.y;
        }
        int left = (int) Math.max(0, Math.floor(minX));
        int top = (int) Math.max(0, Math.floor(minY));
        int width = (int) Math.min(WIDTH, Math.ceil(maxX)) - left;
        int height = (int) Math.min(HEIGHT, Math.ceil(maxY)) - top;
        x /= polygon.length;
        y /= polygon.length;
        int i = shards.size();
        double angle = Math.atan2(y - CENTER.y, x - CENTER.x) + (i % 3 - 1) * .17;
        double speed = .54 + (i * 19 % 43) * .023;
        shards.add(new Shard(polygon, left, top, width, height, x, y,
                Math.cos(angle) * speed, Math.sin(angle) * speed, (i * 7 % 13 - 6) * 13));
    }

    private Image[] bakeShards(Image snapshot) {
        var format = PixelFormat.getIntArgbPreInstance();
        var reader = snapshot.getPixelReader();
        Image[] result = new Image[shards.size()];
        for (int i = 0; i < shards.size(); i++) {
            Shard shard = shards.get(i);
            int[] pixels = new int[shard.width * shard.height];
            reader.getPixels(shard.left, shard.top, shard.width, shard.height, format, pixels, 0, shard.width);
            for (int row = 0; row < shard.height; row++) {
                for (int column = 0; column < shard.width; column++) {
                    if (!contains(shard.polygon, shard.left + column + .5, shard.top + row + .5)) {
                        pixels[row * shard.width + column] = 0;
                    }
                }
            }
            outlineShard(pixels, shard.width, shard.height);
            WritableImage piece = new WritableImage(shard.width, shard.height);
            piece.getPixelWriter().setPixels(0, 0, shard.width, shard.height, format, pixels, 0, shard.width);
            result[i] = piece;
        }
        return result;
    }

    private static void outlineShard(int[] pixels, int width, int height) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = y * width + x;
                int pixel = pixels[i];
                if (pixel == 0) continue;
                if (x == 0 || x == width - 1 || y == 0 || y == height - 1
                        || pixels[i - 1] == 0 || pixels[i + 1] == 0
                        || pixels[i - width] == 0 || pixels[i + width] == 0) {
                    pixels[i] = 0xff000000
                            | (((pixel >>> 16 & 255) + 221) / 2 << 16)
                            | (((pixel >>> 8 & 255) + 138) / 2 << 8)
                            | ((pixel & 255) + 255) / 2;
                }
            }
        }
    }

    private static boolean contains(Point[] polygon, double x, double y) {
        boolean inside = false;
        Point previous = polygon[polygon.length - 1];
        for (Point point : polygon) {
            if ((point.y > y) != (previous.y > y)
                    && x < point.x + (previous.x - point.x) * (y - point.y) / (previous.y - point.y)) {
                inside = !inside;
            }
            previous = point;
        }
        return inside;
    }

    private static Image brighten(Image source) {
        var format = PixelFormat.getIntArgbInstance();
        int width = (int) source.getWidth(), height = (int) source.getHeight();
        int[] pixels = new int[width * height];
        source.getPixelReader().getPixels(0, 0, width, height, format, pixels, 0, width);
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int red = lighten(pixel >>> 16 & 255);
            int green = lighten(pixel >>> 8 & 255);
            int blue = lighten(pixel & 255);
            pixels[i] = (pixel & 0xff000000) | (red << 16) | (green << 8) | blue;
        }
        WritableImage result = new WritableImage(width, height);
        result.getPixelWriter().setPixels(0, 0, width, height, format, pixels, 0, width);
        return result;
    }

    private static int lighten(int channel) {
        return Math.min(255, (int) (channel * 1.55 + 16));
    }

    private static Image makeAura() {
        Canvas canvas = new Canvas(192, 192);
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.setFill(new RadialGradient(0, 0, 96, 96, 96, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.color(.78, .39, 1, .72)),
                new Stop(.45, Color.color(.65, .22, .95, .32)), new Stop(1, Color.TRANSPARENT)));
        graphics.fillRect(0, 0, 192, 192);
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        return canvas.snapshot(parameters, null);
    }

    private void draw(double time) {
        if (screen == null) return;
        graphics.save();
        graphics.clearRect(0, 0, WIDTH, HEIGHT);
        if (time < SHATTER) {
            graphics.drawImage(screen, 0, 0);
            double flash = Math.max(pulse(time, .28), pulse(time, .89));
            if (flash > 0) drawCardFlash(flash);
            if (time >= CRACK) drawCracks(time - CRACK);
        } else {
            graphics.setFill(SHATTER_BACKGROUND);
            graphics.fillRect(0, 0, WIDTH, HEIGHT);
            drawShards(time - SHATTER);
            if (time < IMPACT) {
                double charge = (time - SHATTER) / (IMPACT - SHATTER);
                drawFlight(charge);
            } else {
                double burst = time - IMPACT;
                drawCard(CENTER, finalWidth + 52 * burst, 480 + 36 * burst,
                        0, 1 - clamp(burst / .32), 1);
                drawBurst(burst);
            }
            double flash = .30 * clamp(1 - (time - SHATTER) / .13);
            if (time >= IMPACT) flash += .65 * clamp(1 - (time - IMPACT) / .19);
            graphics.setFill(Color.color(.97, .87, 1, flash));
            graphics.fillRect(0, 0, WIDTH, HEIGHT);
        }
        graphics.restore();
    }

    private void drawCardFlash(double intensity) {
        double x = origin.x - originWidth / 2;
        double y = origin.y - originHeight / 2;
        double sw = card.getWidth(), sh = card.getHeight(), border = .067;
        graphics.save();
        graphics.setGlobalAlpha(intensity);
        graphics.setStroke(CRACK_GLOW);
        graphics.setLineWidth(3 + intensity * 9);
        graphics.strokeRoundRect(x + 3, y + 3, originWidth - 6, originHeight - 6, 11, 11);
        graphics.setStroke(CRACK_COLOR);
        graphics.setLineWidth(1 + intensity * 4);
        graphics.strokeRoundRect(x + 3, y + 3, originWidth - 6, originHeight - 6, 11, 11);
        graphics.drawImage(brightCard, 0, 0, sw, sh * border, x, y, originWidth, originHeight * border);
        graphics.drawImage(brightCard, 0, sh * (1 - border), sw, sh * border,
                x, y + originHeight * (1 - border), originWidth, originHeight * border);
        graphics.drawImage(brightCard, 0, 0, sw * border, sh, x, y, originWidth * border, originHeight);
        graphics.drawImage(brightCard, sw * (1 - border), 0, sw * border, sh,
                x + originWidth * (1 - border), y, originWidth * border, originHeight);
        graphics.restore();
    }

    private void drawCracks(double elapsed) {
        graphics.save();
        for (int i = 0; i < rays.size(); i++) {
            Point[] ray = rays.get(i), next = rays.get((i + 1) % rays.size());
            double delay = (i * 5 % rays.size()) * .046;
            trace(ray, clamp((elapsed - delay) / .38), 2.3);
            trace(new Point[]{ray[1], next[1]}, clamp((elapsed - delay - .13) / .28), 1.3);
            trace(new Point[]{ray[2], next[2]}, clamp((elapsed - delay - .24) / .24), 1.5);
        }
        graphics.restore();
    }

    private void trace(Point[] points, double progress, double width) {
        if (progress <= 0) return;
        graphics.beginPath();
        graphics.moveTo(points[0].x, points[0].y);
        for (int i = 1; i < points.length; i++) {
            double part = clamp(progress * (points.length - 1) - i + 1);
            if (part <= 0) break;
            graphics.lineTo(mix(points[i - 1].x, points[i].x, part), mix(points[i - 1].y, points[i].y, part));
        }
        graphics.setStroke(CRACK_GLOW);
        graphics.setLineWidth(width + 4);
        graphics.stroke();
        graphics.setStroke(CRACK_COLOR);
        graphics.setLineWidth(width);
        graphics.stroke();
    }

    private void drawShards(double elapsed) {
        double p = clamp(elapsed / .63);
        if (p >= 1) return;
        for (int i = 0; i < shards.size(); i++) {
            Shard shard = shards.get(i);
            double distance = 365 * (1 - (1 - p) * (1 - p));
            graphics.save();
            graphics.setGlobalAlpha(1 - clamp((p - .22) / .78));
            graphics.translate(shard.x + shard.dx * distance, shard.y + shard.dy * distance + 145 * p * p);
            graphics.rotate(shard.spin * p);
            graphics.scale(1 - .21 * p, 1 - .33 * p);
            graphics.translate(-shard.x, -shard.y);
            graphics.drawImage(shardImages[i], shard.left, shard.top);
            graphics.restore();
        }
    }

    private void drawFlight(double progress) {
        Flight front = flightAt(progress);
        drawAura(front.position, 88 + front.width, .50 + .44 * progress);
        for (int i = 7; i >= 0; i--) {
            double p = progress - i * .028;
            if (p < 0) continue;
            Flight frame = i == 0 ? front : flightAt(p);
            // The opaque leading card is painted last, above its glow, trail and all shards.
            drawCard(frame.position, frame.width, frame.height, frame.tilt,
                    i == 0 ? 1 : .22 * (1 - i / 9.0), i == 0 ? p : 0);
        }
    }

    private Flight flightAt(double progress) {
        double eased = progress * progress * (3 - 2 * progress);
        double arc = Math.sin(Math.PI * eased);
        return new Flight(new Point(mix(origin.x, CENTER.x, eased),
                mix(origin.y, CENTER.y, eased) - 148 * arc),
                mix(originWidth, finalWidth, eased), mix(originHeight, 480, eased), -19 * arc);
    }

    private void drawAura(Point position, double radius, double alpha) {
        graphics.save();
        graphics.setGlobalAlpha(clamp(alpha));
        graphics.drawImage(aura, position.x - radius, position.y - radius, radius * 2, radius * 2);
        graphics.restore();
    }

    private void drawCard(Point position, double width, double height, double rotation, double alpha, double light) {
        graphics.save();
        graphics.setGlobalAlpha(clamp(alpha));
        graphics.translate(position.x, position.y);
        graphics.rotate(rotation);
        graphics.drawImage(light >= 1 ? brightCard : card, -width / 2, -height / 2, width, height);
        if (light > 0 && light < 1) {
            graphics.setGlobalAlpha(alpha * light);
            graphics.drawImage(brightCard, -width / 2, -height / 2, width, height);
        }
        graphics.restore();
    }

    private void drawBurst(double time) {
        graphics.save();
        drawAura(CENTER, 470 + time * 200, .8 * (1 - time / .4));
        for (int i = 0; i < 2; i++) {
            double p = (time - i * .085) / .52;
            if (p < 0 || p >= 1) continue;
            double radius = 30 + 470 * p;
            graphics.setGlobalAlpha(1 - p);
            graphics.setStroke(i == 0 ? PURPLE : GOLD);
            graphics.setLineWidth(3 + 9 * (1 - p));
            graphics.strokeOval(CENTER.x - radius, CENTER.y - radius, radius * 2, radius * 2);
        }
        for (int i = 0; i < 64; i++) {
            double life = .33 + (i * 13 % 44) * .01;
            if (time >= life) continue;
            double angle = i * 2.39996, distance = 22 + (115 + i * 67 % 450) * time;
            double size = 2 + i * 31 % 8;
            graphics.setGlobalAlpha(1 - time / life);
            graphics.setFill(i % 3 == 0 ? GOLD : PURPLE);
            graphics.fillRect(CENTER.x + Math.cos(angle) * distance,
                    CENTER.y + Math.sin(angle) * distance + 75 * time * time, size, size);
        }
        graphics.restore();
    }

    private static double pulse(double time, double peak) {
        double p = Math.abs(time - peak) / .23;
        return p >= 1 ? 0 : (1 + Math.cos(Math.PI * p)) / 2;
    }

    private static double clamp(double x) { return Math.clamp(x, 0, 1); }
    private static double mix(double a, double b, double t) { return a + (b - a) * t; }
    private record Point(double x, double y) { }
    private record Flight(Point position, double width, double height, double tilt) { }
    private record Shard(Point[] polygon, int left, int top, int width, int height,
                         double x, double y, double dx, double dy, double spin) { }
}
