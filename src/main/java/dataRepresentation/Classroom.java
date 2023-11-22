package dataRepresentation;

import dataModel.AbstractClassroom;
import dataModel.ClassroomAttribute;

import java.util.List;

public class Classroom extends AbstractClassroom {

    private List<String> attributesHeader;

    public Classroom(String name) {
        super(name);
    }

    public List<String> getAttributesHeader() {
        return attributesHeader;
    }

    public void setAttributesHeader(List<String> attributesHeader) {
        this.attributesHeader = attributesHeader;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder("");

        getAttributesHeader().forEach(s -> {
            sb.append(getAttributes().get(s) + " ");
        });

        return this.getName() + " " + sb + "\n";
    }

    @Override
    public boolean equals(Object obj) {
        return getName().equals(obj);
    }
}
