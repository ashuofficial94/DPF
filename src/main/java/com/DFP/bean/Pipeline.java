package com.DFP.bean;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
@Table(name = "piplelines")
public class Pipeline {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    private String name;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String pipelinexml;

    public Pipeline() {
    }

    public Pipeline(String name, String pipelinexml) {
        this.name = name;
        this.pipelinexml = pipelinexml;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPipelinexml() {
        return pipelinexml;
    }

    public void setPipelinexml(String pipelinexml) {
        this.pipelinexml = pipelinexml;
    }
}
