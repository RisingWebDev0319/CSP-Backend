package ca.freshstart.types;

import lombok.Data;

import javax.persistence.Embeddable;

@Data
@Embeddable
public class Duration {
    private Integer prep;
    private Integer processing;
    private Integer clean;

    /**
     * Duration in minutes
     */
    public Integer duration(){
        if(this.prep == null){
            this.prep = new Integer(0);
        }
        return (this.prep + this.processing + this.clean);
    }
}
