package noobgam.me.either;

public final class EitherBool<L,R> {
    private final boolean left;
    private final Object raw;

    private EitherBool(boolean left, Object raw) {
        this.left = left;
        this.raw = raw;
    }

    public static <L, R> EitherBool left(L l) {
        return new EitherBool(true, l);
    }

    public static <L, R> EitherBool right(R r) {
        return new EitherBool(false, r);
    }

    public boolean isLeft() {
        return left;
    }

    public boolean isRight() {
        return !left;
    }

    public L getLeft() {
        if (!left) {
            throw new IllegalArgumentException("Not left");
        }
        return (L)raw;
    }

    public R getRight() {
        if (left) {
            throw new IllegalArgumentException("Not right");
        }
        return (R)raw;
    }
}
