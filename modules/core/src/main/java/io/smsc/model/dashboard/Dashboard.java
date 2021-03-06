package io.smsc.model.dashboard;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.smsc.model.BaseEntity;
import io.smsc.model.admin.User;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import java.util.Set;

/**
 * Specifies Dashboard class as an entity class.
 *
 * @author Nazar Lipkovskyy
 * @see BaseEntity
 * @see User
 * @since 0.0.1-SNAPSHOT
 */
@Entity
@Table(name = "DASHBOARD", uniqueConstraints = {@UniqueConstraint(columnNames = {"NAME"}, name = "dashboards_unique_name_user_idx")})
public class Dashboard extends BaseEntity {

    @Id
    @SequenceGenerator(name = "dashboard_seq", sequenceName = "dashboard_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "dashboard_seq")
    @Column(name = "ID")
    // PROPERTY access for id due to bug: https://hibernate.atlassian.net/browse/HHH-3718
    @Access(value = AccessType.PROPERTY)
    private Long id;

    @Column(name = "NAME", nullable = false, unique = true)
    @NotEmpty(message = "{dashboard.name.validation}")
    private String name;

    @Column(name = "ICON", nullable = false)
    @NotEmpty(message = "{dashboard.icon.validation}")
    private String icon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "USER_ACCOUNT_ID", nullable = false)
    private User user;

    @OneToMany(
            mappedBy = "dashboard",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    @OrderBy("id asc")
    private Set<DashboardBox> dashboardBoxes;

    @JsonIgnore
    public boolean isNew() {
        return getId() == null;
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<DashboardBox> getDashboardBoxes() {
        return dashboardBoxes;
    }

    public void setDashboardBoxes(Set<DashboardBox> dashboardBoxes) {
        this.dashboardBoxes = dashboardBoxes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dashboard dashboard = (Dashboard) o;

        if (!getId().equals(dashboard.getId())) return false;
        if (!getName().equals(dashboard.getName())) return false;
        return getIcon().equals(dashboard.getIcon());
    }

    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + getName().hashCode();
        result = 31 * result + getIcon().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Dashboard{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                "} " + super.toString();
    }
}
