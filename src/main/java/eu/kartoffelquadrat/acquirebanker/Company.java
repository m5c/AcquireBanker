package eu.kartoffelquadrat.acquirebanker;

/**
 * implements the Company interface.
 *
 * @author Max Schiedermeier
 */
public class Company implements CompanyInterface {

    private String companyName;
    //the index is a number from 0 to 6 (depending on array position in super structure)
    private int companyIndex;
    private CompanyType companyType;
    private int size = 0;

    Company(int index, String name, CompanyType type) {
        this.companyIndex = index;
        this.companyName = name;
        this.companyType = type;
    }

    //see interface
    public int getShareValue() {
        return companyType.getValueForSize(size);
    }

    public int getSize() {
        return size;
    }

    public boolean isSave() {
        return (size >= 11);
    }

    /**
     * increases the companies size and therefore influences its stock value
     *
     * @param amount as the number of tiles by which the company grows
     */
    public void increaseCompany(int amount) {
        //System.out.println("increasing by: "+amount);
        size = size + amount;
    }

    public void reset() {
        if (!isActive()) {
            throw new RuntimeException("Cant reset company which is not active");
        }

        if (isSave()) {
            throw new RuntimeException("Can't reset save company.");
        }

        size = 0;
    }

    public boolean equals(String other) {
        return companyName.equals(other);
    }

    public String getName() {
        return companyName;
    }

    public boolean isActive() {
        if (size == 0) {
            return false;
        } else {
            if (size == 1) {
                throw new RuntimeException("A company has size 1");
            } else {
                return true;
            }
        }
    }

    public int getColour() {
        if (companyName.equals("Worldwide")) {
            return 200;
        } else {
            if (companyName.equals("Sackson")) {
                return 208;
            } else {
                if (companyName.equals("Festival")) {
                    return 40;
                } else {
                    if (companyName.equals("Imperial")) {
                        return 184;
                    } else {
                        if (companyName.equals("American")) {
                            return 27;
                        } else {
                            if (companyName.equals("Tower")) {
                                return 253;
                            } else {
                                if (companyName.equals("Continental")) {
                                    return 124;
                                } else {
                                    throw new RuntimeException("Unknown Company: " + companyName);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
