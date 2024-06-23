import java.io.Serializable;

public class Sushi implements Serializable {
    private String name;

    public Sushi(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Sushi{" +
                "name='" + name + '\'' +
                '}';
    }
}
