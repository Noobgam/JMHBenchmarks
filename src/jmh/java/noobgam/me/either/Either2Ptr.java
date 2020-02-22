package noobgam.me.either;

public final class Either2Ptr<L,R> {
    private final L left;
    private final R right;

    private Either2Ptr(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Either2Ptr left(R r) {
        return new Either2Ptr(false, r);
    }

    public static <L, R> Either2Ptr right(L l) {
        return new Either2Ptr(true, l);
    }

    public boolean isLeft() {
        return left != null;
    }

    public boolean isRight() {
        return right != null;
    }

    public L getLeft() {
        if (left == null) {
            throw new IllegalArgumentException("Not left");
        }
        return left;
    }

    public R getRight() {
        if (right == null) {
            throw new IllegalArgumentException("Not right");
        }
        return right;
    }
}
