
package com.annimetsaniitty.cycletracker.model;

import java.time.LocalDate;

public class Cycle {
   private LocalDate startDate;
   private LocalDate endDate;
   
   public Cycle(LocalDate startDate) {
       this.startDate = startDate;
       this.endDate = null;
   }
   
   public LocalDate getStartDate() {
       return startDate;
   }
   
   public void setStartDate(LocalDate startDate) {
       this.startDate = startDate;
   }
   
   public LocalDate getEndDate() {
       return endDate;
   }
    
   public void setEndDate(LocalDate endDate) {
       this.endDate = endDate;
   }
   
   public boolean isActive() {
       return endDate == null;
   }
}
