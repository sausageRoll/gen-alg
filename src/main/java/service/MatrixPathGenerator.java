package service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import model.Matrix;
import model.MatrixPath;
import model.MatrixPoint;

import static service.ListUtils.getRandomElement;
import static service.ListUtils.last;

public class MatrixPathGenerator {

    private MatrixPathValidator matrixPathValidator = new MatrixPathValidator();

    private BresenhamPathCreator bresenhamPathCreator = new BresenhamPathCreator();

    public MatrixPath generatePath(Matrix matrix) {
        return Stream.generate(() -> generateNewPath(matrix))
                .limit(10000)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unable to generate path"));
    }

    private Optional<MatrixPath> generateNewPath(Matrix matrix) {
        List<MatrixPoint> points = new ArrayList<>(matrix.getDimension());

        points.add(matrix.getStart());
        MatrixPoint end = matrix.getEnd();
        int goal = end.getX() + end.getY();
        for (int i = 0; i < 2 * matrix.getDimension() - 2; ++i) {
            MatrixPoint currentPoint = points.get(points.size() - 1);

            if (bothPointsUnavailable(matrix, currentPoint)) {
                return Optional.empty();
            }
            var matrixPoint = chooseRandomNewNextPoint(matrix, currentPoint);
            matrixPoint.ifPresent(points::add);
            if (matrixPoint.isEmpty()) {
                return Optional.empty();
            }

            List<MatrixPoint> nextPossiblePoints = generatePossiblePointsForNextMove(matrix, last(points));
            MatrixPoint randomElement = getRandomElement(nextPossiblePoints);
            addAllPoints(points, randomElement);

            MatrixPath possiblePathToEnd = bresenhamPathCreator.createPath(points.get(points.size() - 1), end);
            if (matrixPathValidator.validateMatrixPath(matrix, possiblePathToEnd)) {
                points.add(end);
                return Optional.of(new MatrixPath(points));
            }
        }

        MatrixPoint last = points.get(points.size() - 1);
        if (!onTheDiagonal(last, end)) {
            return Optional.empty();
        }

        if (possibleToMove(last, end, matrix)) {
            points.add(end);
        } else {
            return Optional.empty();
        }

        return Optional.of(new MatrixPath(points));
    }

    private boolean possibleToMove(MatrixPoint from, MatrixPoint to, Matrix matrix) {
        if (from.getX() == to.getX() && from.getY() == to.getY()) {
            return true;
        }
        if (from.getX() < to.getX()) {
            if (from.getY() < to.getY()) {
                return false;
            }
            if (from.getY() - to.getY() != to.getX() - from.getX()) {
                return false;
            }

            int diff = to.getX() - from.getX();
            for (int i = 1; i <= diff; ++i) {
                if (matrix.get(from.getX() + i, from.getY() - i) != 1) {
                    return false;
                }
            }
            return true;
        } else {
            return possibleToMove(to, from, matrix);
        }
    }

    private void addAllPoints(List<MatrixPoint> points, MatrixPoint matrixPoint) {
        MatrixPoint last = points.get(points.size() - 1);
        if (last == matrixPoint) {
            return;
        }
        if (onTheDiagonal(matrixPoint, last)) {
            int incX = sign(matrixPoint.getX() - last.getX());
            int incY = sign(matrixPoint.getY() - last.getY());

            for (int i = 1; i <= Math.abs(last.getX() - matrixPoint.getX()); ++i) {
                points.add(new MatrixPoint(last.getX() + incX * i, last.getY() + incY * i));
            }
        } else {
            throw new IllegalStateException("unreachable statement");
        }
    }

    private boolean onTheDiagonal(MatrixPoint matrixPoint, MatrixPoint last) {
        return Math.abs(last.getX() - matrixPoint.getX()) == Math.abs(last.getY() - matrixPoint.getY());
    }

    private int sign(int x) {
        return Integer.compare(x, 0);
    }

    private List<MatrixPoint> generatePossiblePointsForNextMove(Matrix matrix, MatrixPoint matrixPoint) {
        List<MatrixPoint> res = new ArrayList<>();
        res.add(matrixPoint);

        int x = matrixPoint.getX() - 1, y = matrixPoint.getY() + 1;
        while (x >= 0 && y < matrix.getDimension() && matrix.get(x, y) != 0) {
            res.add(new MatrixPoint(x, y));
            x -= 1;
            y += 1;
        }

        x = matrixPoint.getX() + 1;
        y = matrixPoint.getY() - 1;
        while (x < matrix.getDimension() && y >= 0 && matrix.get(x, y) != 0) {
            res.add(new MatrixPoint(x, y));
            x += 1;
            y -= 1;
        }

        return res;
    }

    private Optional<MatrixPoint> chooseRandomNewNextPoint(Matrix matrix, MatrixPoint currentPoint) {
        List<MatrixPoint> availablePoints = new ArrayList<>();
        if (currentPoint.getX() + 1 < matrix.getDimension() &&
                matrix.get(currentPoint.getX() + 1, currentPoint.getY()) != 0) {
            availablePoints.add(new MatrixPoint(currentPoint.getX() + 1, currentPoint.getY()));
        }
        if (currentPoint.getY() + 1 < matrix.getDimension() &&
                matrix.get(currentPoint.getX(), currentPoint.getY() + 1) != 0) {
            availablePoints.add(new MatrixPoint(currentPoint.getX(), currentPoint.getY() + 1));
        }
        if (availablePoints.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(getRandomElement(availablePoints));
    }

    private boolean bothPointsUnavailable(Matrix matrix, MatrixPoint currentPoint) {
        return (currentPoint.getX() + 1 < matrix.getDimension()
                && matrix.get(currentPoint.getX() + 1, currentPoint.getY()) == 0)
                && (currentPoint.getY() + 1 < matrix.getDimension()
                && matrix.get(currentPoint.getX(), currentPoint.getY() + 1) == 0);
    }
}
