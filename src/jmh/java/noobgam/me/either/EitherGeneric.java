package noobgam.me.either;

public abstract class EitherGeneric<L,R> {

    public boolean isLeft() {
        return false;
    }

    public boolean isRight() {
        return false;
    }

    public L getLeft() {
        throw new IllegalArgumentException("Not left");
    }

    public R getRight() {
        throw new IllegalArgumentException("Not right");
    }

    public static <L, R> EitherGeneric left(L l) {
        return new Left(l);
    }

    public static <L, R> EitherGeneric right(R r) {
        return new Right(r);
    }

    private static final class Right<L,R> extends EitherGeneric<L,R> {
        private final R val;

        private Right(R val) {
            this.val = val;
        }

        @Override
        public boolean isRight() {
            return true;
        }

        @Override
        public R getRight() {
            return val;
        }
    }

    private static final class Left<L,R> extends EitherGeneric<L,R> {
        private final L val;

        private Left(L val) {
            this.val = val;
        }

        @Override
        public boolean isLeft() {
            return true;
        }

        @Override
        public L getLeft() {
            return val;
        }
    }
}
